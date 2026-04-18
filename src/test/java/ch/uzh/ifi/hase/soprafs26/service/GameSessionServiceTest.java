package ch.uzh.ifi.hase.soprafs26.service;

import ch.uzh.ifi.hase.soprafs26.constant.SessionStatus;
import ch.uzh.ifi.hase.soprafs26.entity.GameSession;
import ch.uzh.ifi.hase.soprafs26.entity.User;
import ch.uzh.ifi.hase.soprafs26.repository.GameSessionRepository;
import ch.uzh.ifi.hase.soprafs26.repository.UserRepository;
import ch.uzh.ifi.hase.soprafs26.rest.dto.SessionGetDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class GameSessionServiceTest {

    @Mock
    private GameSessionRepository sessionRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private SimpMessagingTemplate messagingTemplate;

    @InjectMocks
    private GameSessionService gameSessionService;

    private User creator;
    private User joiner;
    private GameSession session;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);

        creator = new User();
        creator.setId(1L);
        creator.setUsername("host-player");

        joiner = new User();
        joiner.setId(2L);
        joiner.setUsername("guest-player");

        session = new GameSession();
        session.setId(99L);
        session.setCode("ABC123");
        session.setCreatorId(creator.getId());
        session.setCreatorUsername(creator.getUsername());
        session.setStatus(SessionStatus.WAITING);
        session.setCreatedAt(LocalDateTime.now());
    }

    @Test
    public void joinSession_validWaitingSession_success() {
        Mockito.when(userRepository.findById(joiner.getId())).thenReturn(Optional.of(joiner));
        Mockito.when(sessionRepository.findByCode(session.getCode())).thenReturn(session);

        SessionGetDTO result = gameSessionService.joinSession(session.getCode(), joiner.getId());

        assertEquals(joiner.getId(), session.getJoinerId());
        assertEquals(joiner.getUsername(), session.getJoinerUsername());
        assertEquals(2, result.getPlayers().size());
        assertEquals(joiner.getUsername(), result.getPlayers().get(1));
        Mockito.verify(sessionRepository, Mockito.times(1)).flush();
        Mockito.verify(messagingTemplate, Mockito.times(1))
                .convertAndSend(Mockito.eq("/topic/session/" + session.getCode()), Mockito.any(SessionGetDTO.class));
    }

    @Test
    public void joinSession_unknownCode_throwsNotFound() {
        Mockito.when(userRepository.findById(joiner.getId())).thenReturn(Optional.of(joiner));
        Mockito.when(sessionRepository.findByCode("MISSING")).thenReturn(null);

        ResponseStatusException exception =
                assertThrows(ResponseStatusException.class, () -> gameSessionService.joinSession("MISSING", joiner.getId()));

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
    }

    @Test
    public void joinSession_unknownUser_throwsNotFound() {
        Mockito.when(userRepository.findById(joiner.getId())).thenReturn(Optional.empty());

        ResponseStatusException exception =
                assertThrows(ResponseStatusException.class, () -> gameSessionService.joinSession(session.getCode(), joiner.getId()));

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
    }

    @Test
    public void joinSession_creatorJoiningOwnSession_throwsConflict() {
        Mockito.when(userRepository.findById(creator.getId())).thenReturn(Optional.of(creator));
        Mockito.when(sessionRepository.findByCode(session.getCode())).thenReturn(session);

        ResponseStatusException exception =
                assertThrows(ResponseStatusException.class, () -> gameSessionService.joinSession(session.getCode(), creator.getId()));

        assertEquals(HttpStatus.CONFLICT, exception.getStatusCode());
    }

    @Test
    public void joinSession_fullSession_throwsConflict() {
        session.setJoinerId(3L);
        session.setJoinerUsername("other-player");
        Mockito.when(userRepository.findById(joiner.getId())).thenReturn(Optional.of(joiner));
        Mockito.when(sessionRepository.findByCode(session.getCode())).thenReturn(session);

        ResponseStatusException exception =
                assertThrows(ResponseStatusException.class, () -> gameSessionService.joinSession(session.getCode(), joiner.getId()));

        assertEquals(HttpStatus.CONFLICT, exception.getStatusCode());
    }

    @Test
    public void joinSession_activeSession_throwsConflict() {
        session.setStatus(SessionStatus.ACTIVE);
        Mockito.when(userRepository.findById(joiner.getId())).thenReturn(Optional.of(joiner));
        Mockito.when(sessionRepository.findByCode(session.getCode())).thenReturn(session);

        ResponseStatusException exception =
                assertThrows(ResponseStatusException.class, () -> gameSessionService.joinSession(session.getCode(), joiner.getId()));

        assertEquals(HttpStatus.CONFLICT, exception.getStatusCode());
    }

    @Test
    public void joinSession_cancelledSession_throwsConflict() {
        session.setStatus(SessionStatus.CANCELLED);
        Mockito.when(userRepository.findById(joiner.getId())).thenReturn(Optional.of(joiner));
        Mockito.when(sessionRepository.findByCode(session.getCode())).thenReturn(session);

        ResponseStatusException exception =
                assertThrows(ResponseStatusException.class, () -> gameSessionService.joinSession(session.getCode(), joiner.getId()));

        assertEquals(HttpStatus.CONFLICT, exception.getStatusCode());
    }

    @Test
    public void joinSession_expiredWaitingSession_throwsConflict() {
        session.setCreatedAt(LocalDateTime.now().minusMinutes(6));
        Mockito.when(userRepository.findById(joiner.getId())).thenReturn(Optional.of(joiner));
        Mockito.when(sessionRepository.findByCode(session.getCode())).thenReturn(session);

        ResponseStatusException exception =
                assertThrows(ResponseStatusException.class, () -> gameSessionService.joinSession(session.getCode(), joiner.getId()));

        assertEquals(HttpStatus.CONFLICT, exception.getStatusCode());
        assertEquals(SessionStatus.CANCELLED, session.getStatus());
        Mockito.verify(sessionRepository, Mockito.times(1)).flush();
    }

    @Test
    public void startGame_setsStartedAtAndReturnsElapsedSeconds() {
        session.setJoinerId(joiner.getId());
        session.setJoinerUsername(joiner.getUsername());
        Mockito.when(sessionRepository.findByCode(session.getCode())).thenReturn(session);

        SessionGetDTO result = gameSessionService.startGame(session.getCode(), creator.getId());

        assertEquals(SessionStatus.ACTIVE, session.getStatus());
        assertNotNull(session.getStartedAt());
        assertNotNull(result.getStartedAt());
        assertNull(result.getFinishedAt());
        assertNotNull(result.getElapsedSeconds());
        assertTrue(result.getElapsedSeconds() >= 0);
        Mockito.verify(sessionRepository, Mockito.times(1)).flush();
    }

    @Test
    public void getSession_startedSessionReturnsNonNegativeElapsedSeconds() {
        LocalDateTime startedAt = LocalDateTime.now().minusSeconds(12);
        session.setStatus(SessionStatus.ACTIVE);
        session.setStartedAt(startedAt);
        Mockito.when(sessionRepository.findByCode(session.getCode())).thenReturn(session);

        SessionGetDTO result = gameSessionService.getSession(session.getCode());

        assertEquals(startedAt, result.getStartedAt());
        assertNull(result.getFinishedAt());
        assertNotNull(result.getElapsedSeconds());
        assertTrue(result.getElapsedSeconds() >= 12);
    }

    @Test
    public void finishGame_setsFinishedAtAndFreezesElapsedSeconds() {
        LocalDateTime startedAt = LocalDateTime.now().minusSeconds(30);
        session.setJoinerId(joiner.getId());
        session.setJoinerUsername(joiner.getUsername());
        session.setStatus(SessionStatus.ACTIVE);
        session.setStartedAt(startedAt);
        Mockito.when(sessionRepository.findByCode(session.getCode())).thenReturn(session);

        SessionGetDTO firstResult = gameSessionService.finishGame(session.getCode(), joiner.getId());
        SessionGetDTO secondResult = gameSessionService.finishGame(session.getCode(), joiner.getId());

        assertNotNull(session.getFinishedAt());
        assertNotNull(firstResult.getFinishedAt());
        assertNotNull(firstResult.getElapsedSeconds());
        assertEquals(firstResult.getFinishedAt(), secondResult.getFinishedAt());
        assertEquals(firstResult.getElapsedSeconds(), secondResult.getElapsedSeconds());
    }

    @Test
    public void finishGame_nonParticipant_throwsForbidden() {
        User outsider = new User();
        outsider.setId(3L);
        session.setJoinerId(joiner.getId());
        session.setJoinerUsername(joiner.getUsername());
        session.setStatus(SessionStatus.ACTIVE);
        session.setStartedAt(LocalDateTime.now().minusSeconds(10));
        Mockito.when(sessionRepository.findByCode(session.getCode())).thenReturn(session);

        ResponseStatusException exception =
                assertThrows(ResponseStatusException.class, () -> gameSessionService.finishGame(session.getCode(), outsider.getId()));

        assertEquals(HttpStatus.FORBIDDEN, exception.getStatusCode());
    }

    @Test
    public void finishGame_beforeStart_throwsBadRequest() {
        session.setJoinerId(joiner.getId());
        session.setJoinerUsername(joiner.getUsername());
        session.setStatus(SessionStatus.WAITING);
        Mockito.when(sessionRepository.findByCode(session.getCode())).thenReturn(session);

        ResponseStatusException exception =
                assertThrows(ResponseStatusException.class, () -> gameSessionService.finishGame(session.getCode(), creator.getId()));

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
    }
}
