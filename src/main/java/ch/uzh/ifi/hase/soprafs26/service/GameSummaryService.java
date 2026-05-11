package ch.uzh.ifi.hase.soprafs26.service;

import ch.uzh.ifi.hase.soprafs26.constant.SessionStatus;
import ch.uzh.ifi.hase.soprafs26.entity.GameSession;
import ch.uzh.ifi.hase.soprafs26.entity.User;
import ch.uzh.ifi.hase.soprafs26.repository.GameSessionRepository;
import ch.uzh.ifi.hase.soprafs26.repository.UserRepository;
import ch.uzh.ifi.hase.soprafs26.rest.dto.GameSummaryGetDTO;
import ch.uzh.ifi.hase.soprafs26.rest.dto.GameSummaryPostDTO;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;

@Service
public class GameSummaryService {

    private final GameSessionRepository sessionRepository;
    private final UserRepository userRepository;
    private final OpenRouterSummaryService openRouterSummaryService;
    private final TransactionTemplate transactionTemplate;

    public GameSummaryService(
            GameSessionRepository sessionRepository,
            UserRepository userRepository,
            OpenRouterSummaryService openRouterSummaryService,
            TransactionTemplate transactionTemplate) {
        this.sessionRepository = sessionRepository;
        this.userRepository = userRepository;
        this.openRouterSummaryService = openRouterSummaryService;
        this.transactionTemplate = transactionTemplate;
    }

    public GameSummaryGetDTO createSummary(String code, GameSummaryPostDTO request) {
        if (request == null || request.getUserId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Missing userId");
        }

        SummaryPersistenceResult result = transactionTemplate.execute(status -> persistSummary(code, request));
        if (result == null) {
            throw new IllegalStateException("Summary persistence did not return a result");
        }

        String feedback = openRouterSummaryService.generateFeedback(
                result.score(),
                result.elapsedSeconds(),
                result.livesRemaining(),
                result.newHighscore()
        );

        GameSummaryGetDTO response = new GameSummaryGetDTO();
        response.setScore(result.score());
        response.setElapsedSeconds(result.elapsedSeconds());
        response.setNewHighscore(result.newHighscore());
        response.setFeedback(feedback);
        response.setTotalScore(result.totalScore());
        response.setHighestScore(result.highestScore());
        response.setTimePlayed(result.timePlayed());
        return response;
    }

    private SummaryPersistenceResult persistSummary(String code, GameSummaryPostDTO request) {
        GameSession session = findSession(code);
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        validateSummarizableSession(session, user.getId());
        rejectDuplicateSubmission(session, user.getId());

        int score = sanitizeScore(request.getScore());
        long elapsedSeconds = sanitizeElapsedSeconds(request.getElapsedSeconds());
        int livesRemaining = sanitizeLivesRemaining(request.getLivesRemaining());

        if (session.getFinishedAt() == null) {
            session.setFinishedAt(LocalDateTime.now());
        }

        int previousHighscore = user.getHighestScore() == null ? 0 : user.getHighestScore();
        int previousTotalScore = user.getTotalScore() == null ? 0 : user.getTotalScore();
        long previousTimePlayed = user.getTimePlayed() == null ? 0L : user.getTimePlayed();
        boolean newHighscore = score > previousHighscore;

        if (newHighscore) {
            user.setHighestScore(score);
        }
        user.setTotalScore(previousTotalScore + score);
        user.setTimePlayed(previousTimePlayed + elapsedSeconds);
        markSummarySubmitted(session, user.getId());

        sessionRepository.flush();
        userRepository.flush();

        return new SummaryPersistenceResult(
                score,
                elapsedSeconds,
                livesRemaining,
                newHighscore,
                user.getTotalScore(),
                user.getHighestScore(),
                user.getTimePlayed()
        );
    }

    private GameSession findSession(String code) {
        GameSession session = sessionRepository.findByCode(code);
        if (session == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Session with code '" + code + "' not found");
        }
        return session;
    }

    private void validateSummarizableSession(GameSession session, Long userId) {
        if (session.getStatus() == SessionStatus.CANCELLED) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Cancelled sessions cannot be summarized");
        }
        if (session.getStartedAt() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Session has not started yet");
        }
        if (!session.getCreatorId().equals(userId)
                && (session.getJoinerId() == null || !session.getJoinerId().equals(userId))) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only session participants can summarize the game");
        }
    }

    private void rejectDuplicateSubmission(GameSession session, Long userId) {
        if (isCreator(session, userId) && session.isCreatorSummarySubmitted()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Summary already submitted for this user and session");
        }
        if (isJoiner(session, userId) && session.isJoinerSummarySubmitted()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Summary already submitted for this user and session");
        }
    }

    private void markSummarySubmitted(GameSession session, Long userId) {
        if (isCreator(session, userId)) {
            session.setCreatorSummarySubmitted(true);
        }
        else if (isJoiner(session, userId)) {
            session.setJoinerSummarySubmitted(true);
        }
    }

    private boolean isCreator(GameSession session, Long userId) {
        return session.getCreatorId().equals(userId);
    }

    private boolean isJoiner(GameSession session, Long userId) {
        return session.getJoinerId() != null && session.getJoinerId().equals(userId);
    }

    private int sanitizeScore(Integer score) {
        return Math.max(0, score == null ? 0 : score);
    }

    private long sanitizeElapsedSeconds(Long elapsedSeconds) {
        return Math.max(0L, elapsedSeconds == null ? 0L : elapsedSeconds);
    }

    private int sanitizeLivesRemaining(Integer livesRemaining) {
        return Math.max(0, livesRemaining == null ? 0 : livesRemaining);
    }

    private record SummaryPersistenceResult(
            int score,
            long elapsedSeconds,
            int livesRemaining,
            boolean newHighscore,
            int totalScore,
            int highestScore,
            long timePlayed) {
    }
}
