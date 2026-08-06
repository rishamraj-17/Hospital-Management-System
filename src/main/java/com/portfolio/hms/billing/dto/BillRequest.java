package com.portfolio.hms.billing.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.UUID;

public record BillRequest(
        @NotNull(message = "Appointment ID is required")
        UUID appointmentId,

        @NotNull(message = "Consultation fee is required")
        @DecimalMin(value = "0.0", inclusive = true, message = "Consultation fee must be positive")
        BigDecimal consultationFee,

        @NotNull(message = "Medicine charges are required")
        @DecimalMin(value = "0.0", inclusive = true, message = "Medicine charges must be positive")
        BigDecimal medicineCharges,

        @NotNull(message = "Additional charges are required")
        @DecimalMin(value = "0.0", inclusive = true, message = "Additional charges must be positive")
        BigDecimal additionalCharges
) {
}
