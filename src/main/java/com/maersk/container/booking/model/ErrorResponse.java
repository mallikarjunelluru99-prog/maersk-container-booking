package com.maersk.container.booking.model;

import java.util.List;

public record ErrorResponse(String message, String code, List<FieldError> details) {
    public record FieldError(String field, String error) {}
}

