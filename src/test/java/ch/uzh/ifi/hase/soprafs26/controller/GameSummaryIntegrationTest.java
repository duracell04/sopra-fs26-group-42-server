package ch.uzh.ifi.hase.soprafs26.controller;

import ch.uzh.ifi.hase.soprafs26.repository.GameSessionRepository;
import ch.uzh.ifi.hase.soprafs26.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class GameSummaryIntegrationTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private GameSessionRepository sessionRepository;

    @BeforeEach
    public void setup() {
        sessionRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    public void createSummary_fullSessionFlow_returns200ThenDuplicate409() throws Exception {
        Long creatorId = createUser("creator");
        Long joinerId = createUser("joiner");
        String code = createJoinedStartedSession(creatorId, joinerId);

        mockMvc.perform(post("/sessions/{code}/summary", code)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "userId": %d,
                                  "score": 42,
                                  "elapsedSeconds": 93,
                                  "livesRemaining": 1
                                }
                                """.formatted(creatorId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.score", is(42)))
                .andExpect(jsonPath("$.elapsedSeconds", is(93)))
                .andExpect(jsonPath("$.newHighscore", is(true)))
                .andExpect(jsonPath("$.highestScore", is(42)))
                .andExpect(jsonPath("$.totalScore", is(42)))
                .andExpect(jsonPath("$.timePlayed", is(93)));

        mockMvc.perform(post("/sessions/{code}/summary", code)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "userId": %d,
                                  "score": 99,
                                  "elapsedSeconds": 120,
                                  "livesRemaining": 3
                                }
                                """.formatted(creatorId)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.detail", is("Summary already submitted for this user and session")));
    }

    @Test
    public void createSummary_unstartedSession_returns400() throws Exception {
        Long creatorId = createUser("creator");
        String code = createSession(creatorId);

        mockMvc.perform(post("/sessions/{code}/summary", code)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(summaryJson(creatorId)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail", is("Session has not started yet")));
    }

    @Test
    public void createSummary_nonParticipant_returns403() throws Exception {
        Long creatorId = createUser("creator");
        Long joinerId = createUser("joiner");
        Long outsiderId = createUser("outsider");
        String code = createJoinedStartedSession(creatorId, joinerId);

        mockMvc.perform(post("/sessions/{code}/summary", code)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(summaryJson(outsiderId)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.detail", is("Only session participants can summarize the game")));
    }

    @Test
    public void createSummary_missingSession_returns404() throws Exception {
        Long creatorId = createUser("creator");

        mockMvc.perform(post("/sessions/MISSING/summary")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(summaryJson(creatorId)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.detail", is("Session with code 'MISSING' not found")));
    }

    @Test
    public void createSummary_missingUser_returns404() throws Exception {
        Long creatorId = createUser("creator");
        Long joinerId = createUser("joiner");
        String code = createJoinedStartedSession(creatorId, joinerId);

        mockMvc.perform(post("/sessions/{code}/summary", code)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(summaryJson(999999L)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.detail", is("User not found")));
    }

    private Long createUser(String username) throws Exception {
        MvcResult result = mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "username": "%s",
                                  "password": "secret123"
                                }
                                """.formatted(username)))
                .andExpect(status().isCreated())
                .andReturn();
        return readJson(result).path("id").asLong();
    }

    private String createJoinedStartedSession(Long creatorId, Long joinerId) throws Exception {
        String code = createSession(creatorId);

        mockMvc.perform(post("/sessions/{code}/join", code)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "userId": %d
                                }
                                """.formatted(joinerId)))
                .andExpect(status().isOk());

        mockMvc.perform(post("/sessions/{code}/start", code)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "userId": %d
                                }
                                """.formatted(creatorId)))
                .andExpect(status().isOk());

        return code;
    }

    private String createSession(Long creatorId) throws Exception {
        MvcResult result = mockMvc.perform(post("/sessions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "creatorId": %d
                                }
                                """.formatted(creatorId)))
                .andExpect(status().isCreated())
                .andReturn();
        return readJson(result).path("code").asString();
    }

    private String summaryJson(Long userId) {
        return """
                {
                  "userId": %d,
                  "score": 12,
                  "elapsedSeconds": 30,
                  "livesRemaining": 0
                }
                """.formatted(userId);
    }

    private JsonNode readJson(MvcResult result) throws Exception {
        return objectMapper.readTree(result.getResponse().getContentAsString());
    }
}
