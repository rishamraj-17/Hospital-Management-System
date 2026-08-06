package com.portfolio.hms.billing.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record BillResponse(
        UUID id,
        UUID appointmentId,
        BigDecimal consultationFee,
        BigDecimal medicineCharges,
        BigDecimal additionalCharges,
        BigDecimal totalAmount,
        String status
) {
}
