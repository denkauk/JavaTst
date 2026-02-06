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

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class HttpBodyLoggingFilterTest {

    private HttpBodyLoggingFilter filter;
    private ListAppender<ILoggingEvent> listAppender;
    private Logger logger;

    @BeforeEach
    void setUp() {
        filter = new HttpBodyLoggingFilter();
        logger = (Logger) LoggerFactory.getLogger(HttpBodyLoggingFilter.class);
        listAppender = new ListAppender<>();
        listAppender.start();
        logger.addAppender(listAppender);
    }

    @AfterEach
    void tearDown() {
        logger.detachAppender(listAppender);
    }

    @Test
    void testDoFilterInternalLogsAndCopiesBody() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setMethod("POST");
        request.setRequestURI("/test");
        String requestContent = "{\"key\":\"value\"}";
        request.setContent(requestContent.getBytes(StandardCharsets.UTF_8));

        MockHttpServletResponse response = new MockHttpServletResponse();
        String responseContent = "{\"status\":\"ok\"}";

        FilterChain chain = new FilterChain() {
            @Override
            public void doFilter(ServletRequest req, ServletResponse res) throws IOException, ServletException {
                // Read request body to populate cache
                req.getInputStream().readAllBytes();
                // Write response body
                res.getWriter().write(responseContent);
                res.flushBuffer();
            }
        };

        filter.doFilter(request, response, chain);

        assertEquals(200, response.getStatus());
        assertEquals(responseContent, response.getContentAsString());

        List<ILoggingEvent> logs = listAppender.list;
        assertEquals(1, logs.size());
        ILoggingEvent logEvent = logs.get(0);
        assertEquals(Level.INFO, logEvent.getLevel());
        String message = logEvent.getFormattedMessage();
        assertTrue(message.contains("POST /test"));
        assertTrue(message.contains("status=200"));
        assertTrue(message.contains("requestBody=" + requestContent));
        assertTrue(message.contains("responseBody=" + responseContent));
    }

    @Test
    void testEmptyBodies() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setMethod("GET");
        request.setRequestURI("/empty");

        MockHttpServletResponse response = new MockHttpServletResponse();

        MockFilterChain chain = new MockFilterChain();

        filter.doFilter(request, response, chain);

        assertEquals(200, response.getStatus());
        assertEquals("", response.getContentAsString());

        List<ILoggingEvent> logs = listAppender.list;
        assertEquals(1, logs.size());
        ILoggingEvent logEvent = logs.get(0);
        assertEquals(Level.INFO, logEvent.getLevel());
        String message = logEvent.getFormattedMessage();
        assertTrue(message.contains("GET /empty"));
        assertTrue(message.contains("requestBody="));
        assertTrue(message.contains("responseBody="));
    }
}
