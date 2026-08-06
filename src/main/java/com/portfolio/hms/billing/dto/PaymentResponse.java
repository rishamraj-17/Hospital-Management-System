package com.portfolio.hms.billing.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record PaymentResponse(
        UUID id,
        UUID billId,
        BigDecimal amount,
        String paymentMethod,
        String transactionReference,
        Instant paymentDate
) {
}
