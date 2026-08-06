package com.portfolio.hms.appointment.dto;

import java.time.LocalTime;

public record DoctorScheduleResponse(
        LocalTime start,
        LocalTime end,
        String status
) {}
