// src/main/java/com/maersk/containerbooking/exception/GlobalExceptionHandler.java
package com.maersk.container.booking.exception;

import com.fasterxml.jackson.databind.exc.InvalidFormatException;

import com.maersk.container.booking.model.ErrorResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.support.WebExchangeBindException;
import org.springframework.web.server.ServerWebInputException;
import reactor.core.publisher.Mono;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(WebExchangeBindException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Mono<ErrorResponse> handleValidationError(WebExchangeBindException ex) {
        List<ErrorResponse.FieldError> details = ex.getFieldErrors().stream()
                .map(err -> new ErrorResponse.FieldError(err.getField(), err.getDefaultMessage()))
                .collect(Collectors.toList());

        log.warn("Validation failed: {}", details);

        return Mono.just(new ErrorResponse(
                "Invalid request payload",
                "VALIDATION_ERROR",
                details
        ));
    }

    @ExceptionHandler(ServerWebInputException.class)
    public ResponseEntity<ErrorResponse> handleInput(ServerWebInputException ex) {
        List<ErrorResponse.FieldError> details = new ArrayList<>();

        InvalidFormatException ife = findCause(ex, InvalidFormatException.class);

        if (ife != null && ife.getPath() != null && !ife.getPath().isEmpty()) {
            String field = Optional.ofNullable(ife.getPath().get(0).getFieldName()).orElse("requestBody");
            String message = "Invalid value";

            Class<?> target = ife.getTargetType();
            if (target != null && target.isEnum()) {
                message = "must be one of " + Arrays.toString(target.getEnumConstants());
            } else if (ife.getValue() != null) {
                message = "invalid value: " + ife.getValue();
            }

            details.add(new ErrorResponse.FieldError(field, message));
        } else {
            ex.getMethodParameter();
            String field = ex.getMethodParameter().getParameterName();
            String reason = Optional.of(ex.getReason())
                    .orElse("Invalid request payload");
            details.add(new ErrorResponse.FieldError(field, reason));
        }

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponse("Invalid request payload", "VALIDATION_ERROR", details));
    }

    private static <T extends Throwable> T findCause(Throwable t, Class<T> type) {
        while (t != null) {
            if (type.isInstance(t)) return type.cast(t);
            t = t.getCause();
        }
        return null;
    }
}
