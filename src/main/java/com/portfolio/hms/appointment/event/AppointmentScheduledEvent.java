package com.portfolio.hms.appointment.event;

import java.util.UUID;

public record AppointmentScheduledEvent(
        UUID appointmentId,
        UUID patientId,
        UUID doctorId
) {}
