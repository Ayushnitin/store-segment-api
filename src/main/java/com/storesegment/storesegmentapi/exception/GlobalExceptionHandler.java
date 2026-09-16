package com.storesegment.storesegmentapi.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // 422 - VALIDATION ERROR
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<?> handleValidation(
            IllegalArgumentException ex,
            HttpServletRequest request) {

        return buildErrorResponse(
                HttpStatus.UNPROCESSABLE_ENTITY,
                "VALIDATION_ERROR",
                ex.getMessage(),
                request.getRequestURI()
        );
    }

    // 500 - INTERNAL SERVER ERROR
    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> handleGeneralException(
            Exception ex,
            HttpServletRequest request) {

        return buildErrorResponse(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "INTERNAL_ERROR",
                "Unexpected server error.",
                request.getRequestURI()
        );
    }

    private ResponseEntity<?> buildErrorResponse(
            HttpStatus status,
            String code,
            String message,
            String endpoint) {

        Map<String, Object> meta = new LinkedHashMap<>();

        meta.put(
                "request_id",
                "req_" + UUID.randomUUID()
                        .toString()
                        .substring(0, 8)
        );

        meta.put("timestamp", Instant.now().toString());
        meta.put("version", "v1");
        meta.put("end_point", endpoint);

        Map<String, Object> error = new LinkedHashMap<>();

        error.put("code", code);
        error.put("message", message);

        Map<String, Object> response = new LinkedHashMap<>();

        response.put("meta", meta);
        response.put("error", error);

        return ResponseEntity
                .status(status)
                .body(response);
    }
}