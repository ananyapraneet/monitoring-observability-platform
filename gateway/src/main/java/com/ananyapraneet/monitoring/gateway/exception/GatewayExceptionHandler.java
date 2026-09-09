package com.ananyapraneet.monitoring.gateway.exception;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.client.HttpStatusCodeException;

@RestControllerAdvice
public class GatewayExceptionHandler {

    @ExceptionHandler(HttpStatusCodeException.class)
    public ResponseEntity<String> handleDownstreamError(HttpStatusCodeException exception) {
        return ResponseEntity
                .status(exception.getStatusCode())
                .body(exception.getResponseBodyAsString());
    }
}
