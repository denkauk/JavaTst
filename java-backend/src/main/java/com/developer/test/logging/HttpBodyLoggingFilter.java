package com.developer.test.logging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

import javax.servlet.FilterChain;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.DispatcherType;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Component
public class HttpBodyLoggingFilter extends OncePerRequestFilter {
    private static final Logger LOGGER = LoggerFactory.getLogger(HttpBodyLoggingFilter.class);

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain)
            throws ServletException, IOException {

        ContentCachingRequestWrapper req = new ContentCachingRequestWrapper(request);
        ContentCachingResponseWrapper res = new ContentCachingResponseWrapper(response);

        long start = System.currentTimeMillis();

        try {
            chain.doFilter(req, res);
        } finally {
            long duration = System.currentTimeMillis() - start;

            String requestBody = getRequestBody(req);
            String responseBody = getResponseBody(res);
            int status = res.getStatus();

            if (shouldLog(request, status, responseBody)) {
                String path = resolvePath(request);
                LOGGER.info("HTTP {} {}\nstatus={}\ndurationMs={}\nrequestBody={}\nresponseBody={}", request.getMethod(), path, status, duration, requestBody, responseBody);
            }

            res.copyBodyToResponse();
        }
    }

    @Override
    protected boolean shouldNotFilterErrorDispatch() {
        return false;
    }

    private boolean shouldLog(HttpServletRequest request, int status, String responseBody) {
        if (request.getDispatcherType() == DispatcherType.ERROR) {
            return true;
        }

        return status < 500 || !responseBody.isBlank();
    }

    private String resolvePath(HttpServletRequest request) {
        if (request.getDispatcherType() == DispatcherType.ERROR) {
            Object errorPath = request.getAttribute(RequestDispatcher.ERROR_REQUEST_URI);
            if (errorPath instanceof String && !((String) errorPath).isBlank()) {
                return (String) errorPath;
            }
        }
        return request.getRequestURI();
    }

    private String getRequestBody(ContentCachingRequestWrapper req) {
        byte[] buf = req.getContentAsByteArray();
        return buf.length == 0 ? "" : new String(buf, StandardCharsets.UTF_8);
    }

    private String getResponseBody(ContentCachingResponseWrapper res) {
        byte[] buf = res.getContentAsByteArray();
        return buf.length == 0 ? "" : new String(buf, StandardCharsets.UTF_8);
    }
}
