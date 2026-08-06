package com.portfolio.hms.patient.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;

import java.time.LocalDate;

public record PatientRequest(
        @NotBlank String firstName,
        @NotBlank String lastName,
        @NotNull @Past LocalDate dateOfBirth,
        @NotBlank String gender,
        String bloodGroup,
        @NotBlank String phone,
        @Email String email,
        String address,
        String emergencyContactName,
        String emergencyContactPhone,
        String emergencyContactRelationship
) {}
