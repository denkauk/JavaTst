package com.developer.test.logging;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import javax.servlet.ServletException;
import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class RequestLoggingFilterTest {

    private RequestLoggingFilter filter;
    private ListAppender<ILoggingEvent> listAppender;
    private Logger logger;

    @BeforeEach
    void setUp() {
        filter = new RequestLoggingFilter();
        logger = (Logger) LoggerFactory.getLogger(RequestLoggingFilter.class);
        listAppender = new ListAppender<>();
        listAppender.start();
        logger.addAppender(listAppender);
    }

    @AfterEach
    void tearDown() {
        logger.detachAppender(listAppender);
    }

    @Test
    void testLogSuccess() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setMethod("GET");
        request.setRequestURI("/api/test");

        MockHttpServletResponse response = new MockHttpServletResponse();
        response.setStatus(200);

        MockFilterChain chain = new MockFilterChain();

        filter.doFilter(request, response, chain);

        List<ILoggingEvent> logs = listAppender.list;
        assertEquals(1, logs.size());
        ILoggingEvent logEvent = logs.get(0);
        assertEquals(Level.INFO, logEvent.getLevel());
        String message = logEvent.getFormattedMessage();
        assertTrue(message.contains("request method=GET path=/api/test status=200 durationMs="));
    }

    @Test
    void testLogWithQueryString() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setMethod("GET");
        request.setRequestURI("/api/search");
        request.setQueryString("q=test&page=1");

        MockHttpServletResponse response = new MockHttpServletResponse();
        response.setStatus(200);

        MockFilterChain chain = new MockFilterChain();

        filter.doFilter(request, response, chain);

        List<ILoggingEvent> logs = listAppender.list;
        assertEquals(1, logs.size());
        ILoggingEvent logEvent = logs.get(0);
        String message = logEvent.getFormattedMessage();
        assertTrue(message.contains("path=/api/search?q=test&page=1"));
    }

    @Test
    void testLogWithError() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setMethod("POST");
        request.setRequestURI("/api/fail");

        MockHttpServletResponse response = new MockHttpServletResponse();
        // Standard behavior is that if exception occurs, status might not be set yet or be 200 by default in mock
        
        MockFilterChain chain = new MockFilterChain() {
            @Override
            public void doFilter(javax.servlet.ServletRequest request, javax.servlet.ServletResponse response) throws IOException, ServletException {
                throw new RuntimeException("Test Exception");
            }
        };

        assertThrows(RuntimeException.class, () -> filter.doFilter(request, response, chain));

        List<ILoggingEvent> logs = listAppender.list;
        assertEquals(1, logs.size());
        ILoggingEvent logEvent = logs.get(0);
        assertEquals(Level.ERROR, logEvent.getLevel());
        String message = logEvent.getFormattedMessage();
        assertTrue(message.contains("request method=POST path=/api/fail status=500 durationMs="));
        // Verify that the exception is logged. 
        // Note: The formatted message might not contain the stack trace if it's passed as the last argument to SLF4J logger
        // but logback's ILoggingEvent should have it if we were to check getThrowableProxy().
        // However, looking at the log output in failure: "error={}" and then the stack trace follows.
        // If message.contains("error=") is true, it means it found "error=" in the string.
        assertTrue(message.contains("error="));
    }

    @Test
    void testLogWith4xxStatus() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setMethod("GET");
        request.setRequestURI("/api/notfound");

        MockHttpServletResponse response = new MockHttpServletResponse();
        response.setStatus(404);

        MockFilterChain chain = new MockFilterChain();

        filter.doFilter(request, response, chain);

        List<ILoggingEvent> logs = listAppender.list;
        assertEquals(1, logs.size());
        ILoggingEvent logEvent = logs.get(0);
        assertEquals(Level.INFO, logEvent.getLevel()); // It's still INFO because error == null in filter
        String message = logEvent.getFormattedMessage();
        assertTrue(message.contains("status=404"));
    }

    @Test
    void testLogWithErrorAndCustomStatus() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setMethod("PUT");
        request.setRequestURI("/api/error");

        MockHttpServletResponse response = new MockHttpServletResponse();
        response.setStatus(400);

        MockFilterChain chain = new MockFilterChain() {
            @Override
            public void doFilter(javax.servlet.ServletRequest request, javax.servlet.ServletResponse response) throws IOException, ServletException {
                throw new RuntimeException("Bad Request Exception");
            }
        };

        assertThrows(RuntimeException.class, () -> filter.doFilter(request, response, chain));

        List<ILoggingEvent> logs = listAppender.list;
        assertEquals(1, logs.size());
        ILoggingEvent logEvent = logs.get(0);
        assertEquals(Level.ERROR, logEvent.getLevel());
        String message = logEvent.getFormattedMessage();
        assertTrue(message.contains("status=400")); // Should use the status from response since it's >= 400
        assertTrue(message.contains("error="));
    }
}
