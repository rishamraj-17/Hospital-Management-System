package com.portfolio.hms.appointment.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.time.LocalDateTime;
import java.util.UUID;

public record AppointmentRequest(
        @NotNull UUID patientId,
        @NotNull UUID doctorId,
        @NotNull @Future LocalDateTime startTime,
        @NotNull @Positive Integer durationMinutes
) {}
