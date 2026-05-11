package ch.uzh.ifi.hase.soprafs26.service;

import ch.uzh.ifi.hase.soprafs26.constant.SessionStatus;
import ch.uzh.ifi.hase.soprafs26.entity.GameSession;
import ch.uzh.ifi.hase.soprafs26.entity.User;
import ch.uzh.ifi.hase.soprafs26.repository.GameSessionRepository;
import ch.uzh.ifi.hase.soprafs26.repository.UserRepository;
import ch.uzh.ifi.hase.soprafs26.rest.dto.GameSummaryGetDTO;
import ch.uzh.ifi.hase.soprafs26.rest.dto.GameSummaryPostDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class GameSummaryServiceTest {

    private GameSessionRepository sessionRepository;
    private UserRepository userRepository;
    private OpenRouterSummaryService openRouterSummaryService;
    private TransactionTemplate transactionTemplate;
    private GameSummaryService gameSummaryService;
    private GameSession session;
    private User user;

    @BeforeEach
    public void setup() {
        sessionRepository = Mockito.mock(GameSessionRepository.class);
        userRepository = Mockito.mock(UserRepository.class);
        openRouterSummaryService = Mockito.mock(OpenRouterSummaryService.class);
        transactionTemplate = Mockito.mock(TransactionTemplate.class);
        Mockito.when(transactionTemplate.execute(Mockito.any())).thenAnswer(invocation -> {
            TransactionCallback<?> callback = invocation.getArgument(0);
            return callback.doInTransaction(Mockito.mock(TransactionStatus.class));
        });
        gameSummaryService = new GameSummaryService(
                sessionRepository,
                userRepository,
                openRouterSummaryService,
                transactionTemplate
        );

        session = new GameSession();
        session.setId(1L);
        session.setCode("ABC123");
        session.setCreatorId(1L);
        session.setCreatorUsername("host");
        session.setJoinerId(2L);
        session.setJoinerUsername("guest");
        session.setStatus(SessionStatus.ACTIVE);
        session.setCreatedAt(LocalDateTime.now().minusMinutes(2));
        session.setStartedAt(LocalDateTime.now().minusSeconds(93));

        user = new User();
        user.setId(1L);
        user.setUsername("host");
        user.setHighestScore(30);
        user.setTotalScore(138);
        user.setTimePlayed(837L);

        Mockito.when(sessionRepository.findByCode("ABC123")).thenReturn(session);
        Mockito.when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        Mockito.when(openRouterSummaryService.generateFeedback(Mockito.anyInt(), Mockito.anyLong(), Mockito.anyInt(), Mockito.anyBoolean()))
                .thenReturn("Generated feedback.");
    }

    @Test
    public void createSummary_higherScoreUpdatesHighscoreAndStats() {
        GameSummaryPostDTO request = summaryRequest(1L, 42, 93L, 1);

        GameSummaryGetDTO result = gameSummaryService.createSummary("ABC123", request);

        assertTrue(result.getNewHighscore());
        assertEquals(42, result.getHighestScore());
        assertEquals(180, result.getTotalScore());
        assertEquals(930L, result.getTimePlayed());
        assertEquals("Generated feedback.", result.getFeedback());
        assertNotNull(session.getFinishedAt());
        assertTrue(session.isCreatorSummarySubmitted());
        assertFalse(session.isJoinerSummarySubmitted());
        Mockito.verify(sessionRepository).flush();
        Mockito.verify(userRepository).flush();
        Mockito.verify(openRouterSummaryService).generateFeedback(42, 93L, 1, true);
    }

    @Test
    public void createSummary_equalScoreDoesNotSetNewHighscore() {
        user.setHighestScore(42);
        user.setTotalScore(10);
        user.setTimePlayed(20L);
        GameSummaryPostDTO request = summaryRequest(1L, 42, 10L, 0);

        GameSummaryGetDTO result = gameSummaryService.createSummary("ABC123", request);

        assertFalse(result.getNewHighscore());
        assertEquals(42, result.getHighestScore());
        assertEquals(52, result.getTotalScore());
        assertEquals(30L, result.getTimePlayed());
    }

    @Test
    public void createSummary_duplicateSubmissionThrowsConflictWithoutUpdatingStats() {
        session.setCreatorSummarySubmitted(true);
        LocalDateTime previousFinishedAt = session.getFinishedAt();
        GameSummaryPostDTO request = summaryRequest(1L, 99, 120L, 3);

        ResponseStatusException exception =
                assertThrows(ResponseStatusException.class, () -> gameSummaryService.createSummary("ABC123", request));

        assertEquals(HttpStatus.CONFLICT, exception.getStatusCode());
        assertEquals(30, user.getHighestScore());
        assertEquals(138, user.getTotalScore());
        assertEquals(837L, user.getTimePlayed());
        assertEquals(previousFinishedAt, session.getFinishedAt());
        assertTrue(session.isCreatorSummarySubmitted());
        Mockito.verify(sessionRepository, Mockito.never()).flush();
        Mockito.verify(userRepository, Mockito.never()).flush();
        Mockito.verifyNoInteractions(openRouterSummaryService);
    }

    @Test
    public void createSummary_joinerFirstSubmissionMarksJoinerOnly() {
        User joiner = new User();
        joiner.setId(2L);
        joiner.setUsername("guest");
        joiner.setHighestScore(8);
        joiner.setTotalScore(20);
        joiner.setTimePlayed(60L);
        Mockito.when(userRepository.findById(2L)).thenReturn(Optional.of(joiner));
        GameSummaryPostDTO request = summaryRequest(2L, 12, 30L, 0);

        GameSummaryGetDTO result = gameSummaryService.createSummary("ABC123", request);

        assertTrue(result.getNewHighscore());
        assertFalse(session.isCreatorSummarySubmitted());
        assertTrue(session.isJoinerSummarySubmitted());
        assertEquals(32, joiner.getTotalScore());
        assertEquals(90L, joiner.getTimePlayed());
    }

    @Test
    public void createSummary_nonParticipantThrowsForbidden() {
        User outsider = new User();
        outsider.setId(3L);
        Mockito.when(userRepository.findById(3L)).thenReturn(Optional.of(outsider));
        GameSummaryPostDTO request = summaryRequest(3L, 12, 30L, 0);

        ResponseStatusException exception =
                assertThrows(ResponseStatusException.class, () -> gameSummaryService.createSummary("ABC123", request));

        assertEquals(HttpStatus.FORBIDDEN, exception.getStatusCode());
        Mockito.verifyNoInteractions(openRouterSummaryService);
    }

    @Test
    public void createSummary_cancelledSessionThrowsConflict() {
        session.setStatus(SessionStatus.CANCELLED);
        GameSummaryPostDTO request = summaryRequest(1L, 12, 30L, 0);

        ResponseStatusException exception =
                assertThrows(ResponseStatusException.class, () -> gameSummaryService.createSummary("ABC123", request));

        assertEquals(HttpStatus.CONFLICT, exception.getStatusCode());
    }

    @Test
    public void createSummary_unstartedSessionThrowsBadRequest() {
        session.setStartedAt(null);
        GameSummaryPostDTO request = summaryRequest(1L, 12, 30L, 0);

        ResponseStatusException exception =
                assertThrows(ResponseStatusException.class, () -> gameSummaryService.createSummary("ABC123", request));

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
        Mockito.verifyNoInteractions(openRouterSummaryService);
    }

    private GameSummaryPostDTO summaryRequest(Long userId, Integer score, Long elapsedSeconds, Integer livesRemaining) {
        GameSummaryPostDTO request = new GameSummaryPostDTO();
        request.setUserId(userId);
        request.setScore(score);
        request.setElapsedSeconds(elapsedSeconds);
        request.setLivesRemaining(livesRemaining);
        return request;
    }
}
