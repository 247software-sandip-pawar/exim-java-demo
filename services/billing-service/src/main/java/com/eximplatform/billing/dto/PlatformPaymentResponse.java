package com.eximplatform.billing.dto;

import com.eximplatform.billing.domain.PlatformPayment;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record PlatformPaymentResponse(
        UUID id, UUID subscriptionId, UUID companyId, BigDecimal amount, String currency,
        String status, String externalRef, Instant createdAt, Instant updatedAt) {

    public static PlatformPaymentResponse from(PlatformPayment p) {
        return new PlatformPaymentResponse(p.getId(), p.getSubscriptionId(), p.getCompanyId(),
                p.getAmount(), p.getCurrency(), p.getStatus() != null ? p.getStatus().name() : null,
                p.getExternalRef(), p.getCreatedAt(), p.getUpdatedAt());
    }
}
