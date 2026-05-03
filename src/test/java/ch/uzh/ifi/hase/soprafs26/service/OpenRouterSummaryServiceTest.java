package ch.uzh.ifi.hase.soprafs26.service;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class OpenRouterSummaryServiceTest {

    @Test
    public void generateFeedback_missingApiKey_returnsFallbackWithoutCallingClient() throws Exception {
        OpenRouterClient client = Mockito.mock(OpenRouterClient.class);
        OpenRouterSummaryService service = new OpenRouterSummaryService(client, "", "openrouter/free");

        String feedback = service.generateFeedback(42, 93L, 1, true);

        assertEquals(
                "Great run. You scored 42 points in 01:33. Keep focusing on fast factor recognition and avoiding risky shots.",
                feedback
        );
        Mockito.verifyNoInteractions(client);
    }

    @Test
    public void generateFeedback_blankModelDefaultsToFreeRouter() throws Exception {
        OpenRouterClient client = Mockito.mock(OpenRouterClient.class);
        Mockito.when(client.createFeedback(Mockito.eq("secret"), Mockito.eq("openrouter/free"), Mockito.anyString(), Mockito.eq(160)))
                .thenReturn("Strong finish with sharp factor recognition.");
        OpenRouterSummaryService service = new OpenRouterSummaryService(client, "secret", " ");

        String feedback = service.generateFeedback(12, 61L, 2, false);

        assertEquals("Strong finish with sharp factor recognition.", feedback);
    }

    @Test
    public void generateFeedback_openRouterThrows_returnsFallback() throws Exception {
        OpenRouterClient client = Mockito.mock(OpenRouterClient.class);
        Mockito.when(client.createFeedback(Mockito.anyString(), Mockito.anyString(), Mockito.anyString(), Mockito.anyInt()))
                .thenThrow(new RuntimeException("rate limited"));
        OpenRouterSummaryService service = new OpenRouterSummaryService(client, "secret", "openrouter/free");

        String feedback = service.generateFeedback(7, 5L, 0, false);

        assertTrue(feedback.contains("You scored 7 points in 00:05"));
    }

    @Test
    public void generateFeedback_emptyOpenRouterContent_returnsFallback() throws Exception {
        OpenRouterClient client = Mockito.mock(OpenRouterClient.class);
        Mockito.when(client.createFeedback(Mockito.anyString(), Mockito.anyString(), Mockito.anyString(), Mockito.anyInt()))
                .thenReturn("  ");
        OpenRouterSummaryService service = new OpenRouterSummaryService(client, "secret", "openrouter/free");

        String feedback = service.generateFeedback(3, 120L, 0, false);

        assertTrue(feedback.contains("You scored 3 points in 02:00"));
    }
}
