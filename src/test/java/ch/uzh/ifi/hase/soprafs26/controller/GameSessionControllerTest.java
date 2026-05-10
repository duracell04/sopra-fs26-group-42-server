package ch.uzh.ifi.hase.soprafs26.controller;

import ch.uzh.ifi.hase.soprafs26.rest.dto.GameSummaryGetDTO;
import ch.uzh.ifi.hase.soprafs26.rest.dto.SessionGetDTO;
import ch.uzh.ifi.hase.soprafs26.service.GameSessionService;
import ch.uzh.ifi.hase.soprafs26.service.GameSummaryService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(GameSessionController.class)
public class GameSessionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private GameSessionService gameSessionService;

    @MockitoBean
    private GameSummaryService gameSummaryService;

    @Test
    public void createSummary_validRequest_returnsSummaryJson() throws Exception {
        GameSummaryGetDTO response = new GameSummaryGetDTO();
        response.setScore(42);
        response.setElapsedSeconds(93L);
        response.setNewHighscore(true);
        response.setFeedback("Great run.");
        response.setTotalScore(180);
        response.setHighestScore(42);
        response.setTimePlayed(930L);

        given(gameSummaryService.createSummary(eq("ABC123"), any())).willReturn(response);

        mockMvc.perform(post("/sessions/ABC123/summary")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "userId": 1,
                                  "score": 42,
                                  "elapsedSeconds": 93,
                                  "livesRemaining": 1
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.score", is(42)))
                .andExpect(jsonPath("$.elapsedSeconds", is(93)))
                .andExpect(jsonPath("$.newHighscore", is(true)))
                .andExpect(jsonPath("$.feedback", is("Great run.")))
                .andExpect(jsonPath("$.totalScore", is(180)))
                .andExpect(jsonPath("$.highestScore", is(42)))
                .andExpect(jsonPath("$.timePlayed", is(930)));

        Mockito.verify(gameSummaryService).createSummary(eq("ABC123"), any());
    }

    @Test
    public void createSession_validRequest_returnsCreatedSessionJson() throws Exception {
        SessionGetDTO response = sessionResponse("ABC123", 1L, "WAITING", null, "host-player");

        given(gameSessionService.createSession(1L)).willReturn(response);

        mockMvc.perform(post("/sessions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "creatorId": 1
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.code", is("ABC123")))
                .andExpect(jsonPath("$.creatorId", is(1)))
                .andExpect(jsonPath("$.status", is("WAITING")))
                .andExpect(jsonPath("$.players", hasSize(1)))
                .andExpect(jsonPath("$.players[0]", is("host-player")));

        Mockito.verify(gameSessionService).createSession(1L);
    }

    @Test
    public void getSession_validCode_returnsSessionJson() throws Exception {
        SessionGetDTO response = sessionResponse("ABC123", 1L, "WAITING", null, "host-player");

        given(gameSessionService.getSession("ABC123")).willReturn(response);

        mockMvc.perform(get("/sessions/ABC123")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code", is("ABC123")))
                .andExpect(jsonPath("$.creatorId", is(1)))
                .andExpect(jsonPath("$.status", is("WAITING")))
                .andExpect(jsonPath("$.players[0]", is("host-player")));

        Mockito.verify(gameSessionService).getSession("ABC123");
    }

    @Test
    public void getSession_unknownCode_returnsNotFound() throws Exception {
        given(gameSessionService.getSession("MISSING")).willThrow(
                new ResponseStatusException(HttpStatus.NOT_FOUND, "Session with code 'MISSING' not found")
        );

        mockMvc.perform(get("/sessions/MISSING")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());

        Mockito.verify(gameSessionService).getSession("MISSING");
    }

    @Test
    public void joinSession_validRequest_returnsJoinedSessionJson() throws Exception {
        SessionGetDTO response = sessionResponse("ABC123", 1L, "WAITING", null, "host-player", "guest-player");

        given(gameSessionService.joinSession("ABC123", 2L)).willReturn(response);

        mockMvc.perform(post("/sessions/ABC123/join")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "userId": 2
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code", is("ABC123")))
                .andExpect(jsonPath("$.players", hasSize(2)))
                .andExpect(jsonPath("$.players[0]", is("host-player")))
                .andExpect(jsonPath("$.players[1]", is("guest-player")));

        Mockito.verify(gameSessionService).joinSession("ABC123", 2L);
    }

    @Test
    public void joinSession_fullSession_returnsConflict() throws Exception {
        given(gameSessionService.joinSession("ABC123", 3L)).willThrow(
                new ResponseStatusException(HttpStatus.CONFLICT, "Session is already full")
        );

        mockMvc.perform(post("/sessions/ABC123/join")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "userId": 3
                                }
                                """))
                .andExpect(status().isConflict());

        Mockito.verify(gameSessionService).joinSession("ABC123", 3L);
    }

    @Test
    public void cancelSession_validRequest_returnsNoContent() throws Exception {
        mockMvc.perform(delete("/sessions/ABC123")
                        .param("userId", "1"))
                .andExpect(status().isNoContent());

        Mockito.verify(gameSessionService).cancelSession("ABC123", 1L);
    }

    @Test
    public void cancelSession_nonCreator_returnsForbidden() throws Exception {
        Mockito.doThrow(new ResponseStatusException(HttpStatus.FORBIDDEN, "Only the session creator can cancel the session"))
                .when(gameSessionService).cancelSession("ABC123", 2L);

        mockMvc.perform(delete("/sessions/ABC123")
                        .param("userId", "2"))
                .andExpect(status().isForbidden());

        Mockito.verify(gameSessionService).cancelSession("ABC123", 2L);
    }

    @Test
    public void saveProblems_validRequest_returnsNoContent() throws Exception {
        mockMvc.perform(post("/sessions/ABC123/problems")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "problemsJson": "[]"
                                }
                                """))
                .andExpect(status().isNoContent());

        Mockito.verify(gameSessionService).saveProblems("ABC123", "[]");
    }

    @Test
    public void saveProblems_unknownSession_returnsNotFound() throws Exception {
        Mockito.doThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Session with code 'MISSING' not found"))
                .when(gameSessionService).saveProblems("MISSING", "[]");

        mockMvc.perform(post("/sessions/MISSING/problems")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "problemsJson": "[]"
                                }
                                """))
                .andExpect(status().isNotFound());

        Mockito.verify(gameSessionService).saveProblems("MISSING", "[]");
    }

    @Test
    public void getProblems_validRequest_returnsProblemsJson() throws Exception {
        given(gameSessionService.getProblems("ABC123")).willReturn("[]");

        mockMvc.perform(get("/sessions/ABC123/problems")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.problemsJson", is("[]")));

        Mockito.verify(gameSessionService).getProblems("ABC123");
    }

    @Test
    public void getProblems_notGenerated_returnsNotFound() throws Exception {
        given(gameSessionService.getProblems("ABC123")).willThrow(
                new ResponseStatusException(HttpStatus.NOT_FOUND, "Problems not yet generated")
        );

        mockMvc.perform(get("/sessions/ABC123/problems")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());

        Mockito.verify(gameSessionService).getProblems("ABC123");
    }

    @Test
    public void startGame_validRequest_returnsActiveSession() throws Exception {
        SessionGetDTO response = sessionResponse("ABC123", 1L, "ACTIVE", 0L, "host-player", "guest-player");
        response.setStartedAt(LocalDateTime.of(2026, 5, 7, 16, 1, 0));

        given(gameSessionService.startGame("ABC123", 1L)).willReturn(response);

        mockMvc.perform(post("/sessions/ABC123/start")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "userId": 1
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("ACTIVE")))
                .andExpect(jsonPath("$.elapsedSeconds", is(0)))
                .andExpect(jsonPath("$.players", hasSize(2)));

        Mockito.verify(gameSessionService).startGame("ABC123", 1L);
    }

    @Test
    public void startGame_nonCreator_returnsForbidden() throws Exception {
        given(gameSessionService.startGame("ABC123", 2L)).willThrow(
                new ResponseStatusException(HttpStatus.FORBIDDEN, "Only the session creator can start the game")
        );

        mockMvc.perform(post("/sessions/ABC123/start")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "userId": 2
                                }
                                """))
                .andExpect(status().isForbidden());

        Mockito.verify(gameSessionService).startGame("ABC123", 2L);
    }

    @Test
    public void startGame_missingSecondPlayer_returnsBadRequest() throws Exception {
        given(gameSessionService.startGame("ABC123", 1L)).willThrow(
                new ResponseStatusException(HttpStatus.BAD_REQUEST, "Need two players to start the game")
        );

        mockMvc.perform(post("/sessions/ABC123/start")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "userId": 1
                                }
                                """))
                .andExpect(status().isBadRequest());

        Mockito.verify(gameSessionService).startGame("ABC123", 1L);
    }

    @Test
    public void finishGame_validRequest_returnsFinishedSession() throws Exception {
        SessionGetDTO response = sessionResponse("ABC123", 1L, "ACTIVE", 93L, "host-player", "guest-player");
        response.setStartedAt(LocalDateTime.of(2026, 5, 7, 16, 1, 0));
        response.setFinishedAt(LocalDateTime.of(2026, 5, 7, 16, 2, 33));

        given(gameSessionService.finishGame("ABC123", 2L)).willReturn(response);

        mockMvc.perform(post("/sessions/ABC123/finish")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "userId": 2
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("ACTIVE")))
                .andExpect(jsonPath("$.elapsedSeconds", is(93)))
                .andExpect(jsonPath("$.players", hasSize(2)));

        Mockito.verify(gameSessionService).finishGame("ABC123", 2L);
    }

    @Test
    public void finishGame_cancelledSession_returnsConflict() throws Exception {
        given(gameSessionService.finishGame("ABC123", 1L)).willThrow(
                new ResponseStatusException(HttpStatus.CONFLICT, "Cancelled sessions cannot be finished")
        );

        mockMvc.perform(post("/sessions/ABC123/finish")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "userId": 1
                                }
                                """))
                .andExpect(status().isConflict());

        Mockito.verify(gameSessionService).finishGame("ABC123", 1L);
    }

    @Test
    public void finishGame_nonParticipant_returnsForbidden() throws Exception {
        given(gameSessionService.finishGame("ABC123", 99L)).willThrow(
                new ResponseStatusException(HttpStatus.FORBIDDEN, "Only session participants can finish the game")
        );

        mockMvc.perform(post("/sessions/ABC123/finish")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "userId": 99
                                }
                                """))
                .andExpect(status().isForbidden());

        Mockito.verify(gameSessionService).finishGame("ABC123", 99L);
    }

    private SessionGetDTO sessionResponse(
            String code,
            Long creatorId,
            String status,
            Long elapsedSeconds,
            String... players
    ) {
        SessionGetDTO dto = new SessionGetDTO();
        dto.setId(99L);
        dto.setCode(code);
        dto.setCreatorId(creatorId);
        dto.setStatus(status);
        dto.setPlayers(List.of(players));
        dto.setCreatedAt(LocalDateTime.of(2026, 5, 7, 16, 0, 0));
        dto.setExpiresAt(LocalDateTime.of(2026, 5, 7, 16, 5, 0));
        dto.setElapsedSeconds(elapsedSeconds);
        return dto;
    }
}
