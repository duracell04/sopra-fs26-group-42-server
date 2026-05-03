package ch.uzh.ifi.hase.soprafs26.controller;

import ch.uzh.ifi.hase.soprafs26.rest.dto.GameSummaryGetDTO;
import ch.uzh.ifi.hase.soprafs26.service.GameSessionService;
import ch.uzh.ifi.hase.soprafs26.service.GameSummaryService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
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
}
