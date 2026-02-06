package com.developer.test.logging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
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

            LOGGER.info("HTTP {} {}\nstatus={}\ndurationMs={}\nrequestBody={}\nresponseBody={}",
                    request.getMethod(),
                    request.getRequestURI(),
                    res.getStatus(),
                    duration,
                    requestBody,
                    responseBody
            );

            res.copyBodyToResponse();
        }
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
