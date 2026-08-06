package com.portfolio.hms.auth.dto;

import com.portfolio.hms.auth.enums.Role;
import java.time.Instant;
import java.util.UUID;

public record UserResponse(
    UUID id,
    String username,
    String email,
    Role role,
    Instant createdAt
) {}
