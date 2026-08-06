package com.portfolio.hms.appointment.dto;

import com.portfolio.hms.appointment.enums.AppointmentStatus;

import java.time.LocalDateTime;
import java.util.UUID;

public record AppointmentResponse(
        UUID id,
        UUID patientId,
        UUID doctorId,
        LocalDateTime startTime,
        Integer durationMinutes,
        LocalDateTime endTime,
        AppointmentStatus status
) {}
