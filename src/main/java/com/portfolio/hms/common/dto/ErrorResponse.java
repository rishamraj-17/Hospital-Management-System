package com.portfolio.hms.common.dto;

import java.time.Instant;
import java.util.Map;

public record ErrorResponse(
    int status,
    String message,
    Instant timestamp,
    Map<String, String> errors
) {
    public ErrorResponse {
        errors = errors == null ? Map.of() : Map.copyOf(errors);
    }
}
