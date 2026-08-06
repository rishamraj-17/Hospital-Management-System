package com.portfolio.hms.patient.dto;

import java.time.LocalDate;
import java.util.UUID;

public record PatientResponse(
        UUID id,
        String mrn,
        String firstName,
        String lastName,
        LocalDate dateOfBirth,
        String gender,
        String bloodGroup,
        String phone,
        String email,
        String address,
        String emergencyContactName,
        String emergencyContactPhone,
        String emergencyContactRelationship
) {}
