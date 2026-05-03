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
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;

@Service
@Transactional
public class GameSummaryService {

    private final GameSessionRepository sessionRepository;
    private final UserRepository userRepository;
    private final OpenRouterSummaryService openRouterSummaryService;

    public GameSummaryService(
            GameSessionRepository sessionRepository,
            UserRepository userRepository,
            OpenRouterSummaryService openRouterSummaryService) {
        this.sessionRepository = sessionRepository;
        this.userRepository = userRepository;
        this.openRouterSummaryService = openRouterSummaryService;
    }

    public GameSummaryGetDTO createSummary(String code, GameSummaryPostDTO request) {
        if (request == null || request.getUserId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Missing userId");
        }

        GameSession session = findSession(code);
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        validateSummarizableSession(session, user.getId());

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

        sessionRepository.flush();
        userRepository.flush();

        String feedback = openRouterSummaryService.generateFeedback(
                score,
                elapsedSeconds,
                livesRemaining,
                newHighscore
        );

        GameSummaryGetDTO response = new GameSummaryGetDTO();
        response.setScore(score);
        response.setElapsedSeconds(elapsedSeconds);
        response.setNewHighscore(newHighscore);
        response.setFeedback(feedback);
        response.setTotalScore(user.getTotalScore());
        response.setHighestScore(user.getHighestScore());
        response.setTimePlayed(user.getTimePlayed());
        return response;
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

    private int sanitizeScore(Integer score) {
        return Math.max(0, score == null ? 0 : score);
    }

    private long sanitizeElapsedSeconds(Long elapsedSeconds) {
        return Math.max(0L, elapsedSeconds == null ? 0L : elapsedSeconds);
    }

    private int sanitizeLivesRemaining(Integer livesRemaining) {
        return Math.max(0, livesRemaining == null ? 0 : livesRemaining);
    }
}
