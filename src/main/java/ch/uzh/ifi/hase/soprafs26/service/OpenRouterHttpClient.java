package ch.uzh.ifi.hase.soprafs26.service;

import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;
import java.util.Map;

@Component
public class OpenRouterHttpClient implements OpenRouterClient {

    private static final URI CHAT_COMPLETIONS_URI = URI.create("https://openrouter.ai/api/v1/chat/completions");
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    public OpenRouterHttpClient() {
        this(HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5)).build(), new ObjectMapper());
    }

    OpenRouterHttpClient(HttpClient httpClient, ObjectMapper objectMapper) {
        this.httpClient = httpClient;
        this.objectMapper = objectMapper;
    }

    @Override
    public String createFeedback(String apiKey, String model, String prompt, int maxTokens) throws Exception {
        Map<String, Object> payload = Map.of(
                "model", model,
                "max_tokens", maxTokens,
                "temperature", 0.4,
                "messages", List.of(
                        Map.of(
                                "role", "system",
                                "content", "You write short encouraging Math Invaders end-game feedback. Return one concise sentence, max 35 words. Do not mention being AI."),
                        Map.of("role", "user", "content", prompt)
                )
        );

        HttpRequest request = HttpRequest.newBuilder(CHAT_COMPLETIONS_URI)
                .timeout(Duration.ofSeconds(10))
                .header("Authorization", "Bearer " + apiKey)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(payload)))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() < 200 || response.statusCode() >= 300) {
            throw new IOException("OpenRouter request failed with status " + response.statusCode());
        }

        JsonNode root = objectMapper.readTree(response.body());
        String content = root.path("choices").path(0).path("message").path("content").asText("").trim();
        if (content.isBlank()) {
            throw new IOException("OpenRouter response did not include feedback content");
        }

        return content;
    }
}
