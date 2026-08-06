package com.portfolio.hms.clinical.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record DoctorRequest(
        @NotNull(message = "User ID is required")
        UUID userId,

        @NotNull(message = "Department ID is required")
        UUID departmentId,

        @NotBlank(message = "License number is required")
        @Size(max = 100)
        String licenseNumber,

        @NotBlank(message = "Email is required")
        @Email(message = "Invalid email format")
        String email,

        String phone,

        @NotBlank(message = "Specialization is required")
        String specialization,

        @Min(value = 0, message = "Experience years cannot be negative")
        int experienceYears
) {}
