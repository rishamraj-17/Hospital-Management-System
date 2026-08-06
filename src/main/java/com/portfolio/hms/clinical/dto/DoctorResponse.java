package com.portfolio.hms.clinical.dto;

import java.time.Instant;
import java.util.UUID;

public record DoctorResponse(
        UUID id,
        UUID userId,
        String username,
        UUID departmentId,
        String departmentName,
        String licenseNumber,
        String email,
        String phone,
        String specialization,
        int experienceYears,
        Instant createdAt,
        Instant updatedAt
) {}
