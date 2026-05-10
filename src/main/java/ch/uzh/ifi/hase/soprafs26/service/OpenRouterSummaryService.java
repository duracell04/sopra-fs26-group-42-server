package ch.uzh.ifi.hase.soprafs26.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class OpenRouterSummaryService {

    private static final Logger LOGGER = LoggerFactory.getLogger(OpenRouterSummaryService.class);
    private static final String DEFAULT_MODEL = "openrouter/free";
    private static final int MAX_TOKENS = 160;

    private final OpenRouterClient openRouterClient;
    private final String apiKey;
    private final String model;

    public OpenRouterSummaryService(
            OpenRouterClient openRouterClient,
            @Value("${OPENROUTER_API_KEY:}") String apiKey,
            @Value("${OPENROUTER_MODEL:openrouter/free}") String model) {
        this.openRouterClient = openRouterClient;
        this.apiKey = apiKey;
        this.model = normalizeModel(model);
    }

    public String generateFeedback(int score, long elapsedSeconds, int livesRemaining, boolean newHighscore) {
        if (apiKey == null || apiKey.isBlank()) {
            LOGGER.warn("OPENROUTER_API_KEY is not configured; returning deterministic game summary feedback.");
            return fallbackFeedback(score, elapsedSeconds);
        }

        try {
            String feedback = openRouterClient.createFeedback(
                    apiKey,
                    model,
                    buildPrompt(score, elapsedSeconds, livesRemaining, newHighscore),
                    MAX_TOKENS
            );
            if (feedback == null || feedback.isBlank()) {
                LOGGER.warn("OpenRouter returned blank feedback for model {}; returning deterministic game summary feedback.", model);
                return fallbackFeedback(score, elapsedSeconds);
            }
            return feedback.trim();
        } catch (Exception error) {
            if (error instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }
            LOGGER.warn(
                    "OpenRouter feedback generation failed for model {} ({}: {}); returning deterministic game summary feedback.",
                    model,
                    error.getClass().getSimpleName(),
                    error.getMessage()
            );
            return fallbackFeedback(score, elapsedSeconds);
        }
    }

    String fallbackFeedback(int score, long elapsedSeconds) {
        return String.format(
                "Great run. You scored %d points in %s. Keep focusing on fast factor recognition and avoiding risky shots.",
                score,
                formatDuration(elapsedSeconds)
        );
    }

    private String buildPrompt(int score, long elapsedSeconds, int livesRemaining, boolean newHighscore) {
        return String.format(
                "Write short Math Invaders feedback for a player. Score: %d. Time: %s. Lives remaining: %d. New highscore: %s.",
                score,
                formatDuration(elapsedSeconds),
                livesRemaining,
                newHighscore ? "yes" : "no"
        );
    }

    private String formatDuration(long elapsedSeconds) {
        long safeSeconds = Math.max(0, elapsedSeconds);
        long minutes = safeSeconds / 60;
        long seconds = safeSeconds % 60;
        return String.format("%02d:%02d", minutes, seconds);
    }

    private String normalizeModel(String configuredModel) {
        if (configuredModel == null || configuredModel.isBlank()) {
            return DEFAULT_MODEL;
        }
        return configuredModel.trim();
    }
}
