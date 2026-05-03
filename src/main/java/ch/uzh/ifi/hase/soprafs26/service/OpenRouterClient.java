package ch.uzh.ifi.hase.soprafs26.service;

public interface OpenRouterClient {
    String createFeedback(String apiKey, String model, String prompt, int maxTokens) throws Exception;
}
