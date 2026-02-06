package com.developer.test.logging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

@Component
public class RequestLoggingFilter extends OncePerRequestFilter {
    private static final Logger LOGGER = LoggerFactory.getLogger(RequestLoggingFilter.class);

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {

        long startNanos = System.nanoTime();
        Exception error = null;

        try {
            chain.doFilter(request, response);
        } catch (Exception ex) {
            error = ex;
            throw ex;
        } finally {
            long durationMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startNanos);
            String path = buildPath(request);
            int status = response.getStatus();
            if (error == null) {
                LOGGER.info("request method={} path={} status={} durationMs={}", request.getMethod(), path, status, durationMs);
            } else {
                int errorStatus = status >= 400 ? status : 500;
                LOGGER.error("request method={} path={} status={} durationMs={} error={}", request.getMethod(), path, errorStatus, durationMs, error, error);
            }
        }
    }

    private String buildPath(HttpServletRequest request) {
        String path = request.getRequestURI();
        String query = request.getQueryString();
        return (query == null || query.isBlank()) ? path : path + "?" + query;
    }
}
