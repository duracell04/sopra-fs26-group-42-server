package ch.uzh.ifi.hase.soprafs26.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    private static final Logger LOGGER = LoggerFactory.getLogger(OpenRouterHttpClient.class);
    private static final URI CHAT_COMPLETIONS_URI = URI.create("https://openrouter.ai/api/v1/chat/completions");
    private static final List<String> CONTENT_TEXT_FIELDS = List.of("text", "content", "value", "output_text");
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
        JsonNode firstChoice = root.path("choices").path(0);
        JsonNode message = firstChoice.path("message");
        String content = extractContent(message.path("content")).trim();
        if (content.isBlank()) {
            LOGGER.warn(
                    "OpenRouter response did not include feedback content. responseModel={}, finishReason={}, messageFields={}",
                    root.path("model").asString(""),
                    firstChoice.path("finish_reason").asString(""),
                    fieldNames(message)
            );
            throw new IOException("OpenRouter response did not include feedback content");
        }

        return content;
    }

    private String extractContent(JsonNode contentNode) {
        if (contentNode.isString()) {
            return contentNode.asString("");
        }
        if (contentNode.isArray()) {
            StringBuilder content = new StringBuilder();
            for (JsonNode part : contentNode) {
                String text = part.path("text").asString("").trim();
                if (!text.isBlank()) {
                    if (content.length() > 0) {
                        content.append(" ");
                    }
                    content.append(text);
                }
            }
            return content.toString();
        }
        if (contentNode.isObject()) {
            for (String fieldName : CONTENT_TEXT_FIELDS) {
                String text = contentNode.path(fieldName).asString("").trim();
                if (!text.isBlank()) {
                    return text;
                }
            }
        }
        return "";
    }

    private List<String> fieldNames(JsonNode node) {
        return node.properties().stream()
                .map(Map.Entry::getKey)
                .toList();
    }
}
