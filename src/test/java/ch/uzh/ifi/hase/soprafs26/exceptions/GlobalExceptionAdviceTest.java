package ch.uzh.ifi.hase.soprafs26.exceptions;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.transaction.TransactionSystemException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.server.ResponseStatusException;

class GlobalExceptionAdviceTest {

    private final GlobalExceptionAdvice advice = new GlobalExceptionAdvice();

    @Test
    void handleConflict_illegalArgument_returnsConflictStatus() {
        IllegalArgumentException ex = new IllegalArgumentException("bad argument");
        WebRequest request = new ServletWebRequest(new MockHttpServletRequest());

        ResponseEntity<Object> response = advice.handleConflict(ex, request);

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
    }

    @Test
    void handleConflict_illegalState_returnsConflictStatus() {
        IllegalStateException ex = new IllegalStateException("bad state");
        WebRequest request = new ServletWebRequest(new MockHttpServletRequest());

        ResponseEntity<Object> response = advice.handleConflict(ex, request);

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
    }

    @Test
    void handleTransactionSystemException_returnsConflict() {
        TransactionSystemException ex = new TransactionSystemException("transaction failed");
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/api/test");

        ResponseStatusException result = advice.handleTransactionSystemException(ex, request);

        assertEquals(HttpStatus.CONFLICT, result.getStatusCode());
    }

    @Test
    void handleException_returnsInternalServerError() {
        HttpServerErrorException ex = new HttpServerErrorException(HttpStatus.INTERNAL_SERVER_ERROR, "server error");

        ResponseStatusException result = advice.handleException(ex);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, result.getStatusCode());
    }
}
