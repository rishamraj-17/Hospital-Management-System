package com.portfolio.hms.auth.dto;

public record AuthenticationResponse(
    String accessToken,
    UserResponse user
) {}
