package ch.uzh.ifi.hase.soprafs26.service;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;

public class OpenRouterHttpClientTest {

    @Test
    public void createFeedback_stringContent_returnsTrimmedFeedback() throws Exception {
        OpenRouterHttpClient client = clientReturning(200, """
                {
                  "model": "openrouter/free",
                  "choices": [
                    {
                      "finish_reason": "stop",
                      "message": {
                        "role": "assistant",
                        "content": "  Sharp factor work kept your run moving.  "
                      }
                    }
                  ]
                }
                """);

        String feedback = client.createFeedback("secret", "openrouter/free", "prompt", 160);

        assertEquals("Sharp factor work kept your run moving.", feedback);
    }

    @Test
    public void createFeedback_arrayContent_concatenatesTextParts() throws Exception {
        OpenRouterHttpClient client = clientReturning(200, """
                {
                  "model": "openrouter/free",
                  "choices": [
                    {
                      "finish_reason": "stop",
                      "message": {
                        "role": "assistant",
                        "content": [
                          { "type": "text", "text": "Strong score." },
                          { "type": "text", "text": "Keep recognizing factors quickly." }
                        ]
                      }
                    }
                  ]
                }
                """);

        String feedback = client.createFeedback("secret", "openrouter/free", "prompt", 160);

        assertEquals("Strong score. Keep recognizing factors quickly.", feedback);
    }

    @Test
    public void createFeedback_objectContent_usesCommonTextField() throws Exception {
        OpenRouterHttpClient client = clientReturning(200, """
                {
                  "model": "openrouter/free",
                  "choices": [
                    {
                      "finish_reason": "stop",
                      "message": {
                        "role": "assistant",
                        "content": {
                          "output_text": "Great survival under pressure."
                        }
                      }
                    }
                  ]
                }
                """);

        String feedback = client.createFeedback("secret", "openrouter/free", "prompt", 160);

        assertEquals("Great survival under pressure.", feedback);
    }

    @Test
    public void createFeedback_blankContent_throwsFeedbackContentIOException() throws Exception {
        OpenRouterHttpClient client = clientReturning(200, """
                {
                  "model": "openrouter/free",
                  "choices": [
                    {
                      "finish_reason": "stop",
                      "message": {
                        "role": "assistant",
                        "content": "   "
                      }
                    }
                  ]
                }
                """);

        IOException error = assertThrows(
                IOException.class,
                () -> client.createFeedback("secret", "openrouter/free", "prompt", 160)
        );

        assertEquals("OpenRouter response did not include feedback content", error.getMessage());
    }

    @Test
    public void createFeedback_missingContent_throwsFeedbackContentIOException() throws Exception {
        OpenRouterHttpClient client = clientReturning(200, """
                {
                  "model": "openrouter/free",
                  "choices": [
                    {
                      "finish_reason": "stop",
                      "message": {
                        "role": "assistant"
                      }
                    }
                  ]
                }
                """);

        IOException error = assertThrows(
                IOException.class,
                () -> client.createFeedback("secret", "openrouter/free", "prompt", 160)
        );

        assertEquals("OpenRouter response did not include feedback content", error.getMessage());
    }

    @Test
    public void createFeedback_non2xxResponse_throwsStatusIOException() throws Exception {
        OpenRouterHttpClient client = clientReturning(429, """
                { "error": { "message": "rate limited" } }
                """);

        IOException error = assertThrows(
                IOException.class,
                () -> client.createFeedback("secret", "openrouter/free", "prompt", 160)
        );

        assertEquals("OpenRouter request failed with status 429", error.getMessage());
    }

    @SuppressWarnings("unchecked")
    private OpenRouterHttpClient clientReturning(int status, String body) throws Exception {
        HttpClient httpClient = Mockito.mock(HttpClient.class);
        HttpResponse<String> response = Mockito.mock(HttpResponse.class);
        Mockito.when(response.statusCode()).thenReturn(status);
        Mockito.when(response.body()).thenReturn(body);
        Mockito.when(httpClient.send(any(HttpRequest.class), Mockito.<HttpResponse.BodyHandler<String>>any()))
                .thenReturn(response);

        return new OpenRouterHttpClient(httpClient, new ObjectMapper());
    }
}
