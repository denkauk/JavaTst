/* Copyright © 2026 EIS Group and/or one of its affiliates. All rights reserved. Unpublished work under U.S. copyright laws.
CONFIDENTIAL AND TRADE SECRET INFORMATION. No portion of this work may be copied, distributed, modified, or incorporated into any other media without EIS Group prior written consent.*/
package com.developer.test.controller;


import com.developer.test.model.Task;
import com.fasterxml.jackson.databind.exc.ValueInstantiationException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

import static org.springframework.core.NestedExceptionUtils.getRootCause;

@RestControllerAdvice
public class JsonErrorHandler {

    @ExceptionHandler(ValueInstantiationException.class)
    public ResponseEntity<?> handleEnumError(ValueInstantiationException ex) {

        Throwable root = getRootCause(ex);
        if (root instanceof IllegalArgumentException &&
                root.getMessage().startsWith("Invalid status")) {

            return ResponseEntity.badRequest().body(
                    Map.of(
                            "error", "Invalid status",
                            "allowed", Task.Status.allowed()
                    )
            );
        }

        return ResponseEntity.badRequest().body(
                Map.of("error", "Invalid request body")
        );
    }
}
