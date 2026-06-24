package com.eximplatform.billing.dto;

import com.eximplatform.billing.domain.Subscription;

import java.time.Instant;
import java.util.UUID;

public class SubscriptionResponse {

    private UUID id;
    private UUID companyId;
    private String planCode;
    private String status;
    private Instant startedAt;
    private Instant currentPeriodEnd;
    private boolean autoRenew;
    private Instant createdAt;
    private Instant updatedAt;

    public static SubscriptionResponse from(Subscription s) {
        SubscriptionResponse out = new SubscriptionResponse();
        out.id = s.getId();
        out.companyId = s.getCompanyId();
        out.planCode = s.getPlanCode();
        out.status = s.getStatus() != null ? s.getStatus().name() : null;
        out.startedAt = s.getStartedAt();
        out.currentPeriodEnd = s.getCurrentPeriodEnd();
        out.autoRenew = s.isAutoRenew();
        out.createdAt = s.getCreatedAt();
        out.updatedAt = s.getUpdatedAt();
        return out;
    }

    public UUID getId() { return id; }
    public UUID getCompanyId() { return companyId; }
    public String getPlanCode() { return planCode; }
    public String getStatus() { return status; }
    public Instant getStartedAt() { return startedAt; }
    public Instant getCurrentPeriodEnd() { return currentPeriodEnd; }
    public boolean isAutoRenew() { return autoRenew; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
}
