package com.eximplatform.billing.domain;

import com.eximplatform.common.domain.BaseEntity;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.UUID;

/**
 * A company's subscription to a plan. {@code companyId} links to the identity service by bare UUID;
 * {@code planCode} references a {@link Plan}. A company has at most one ACTIVE subscription.
 */
@Document(collection = "subscriptions")
public class Subscription extends BaseEntity {

    @Indexed
    private UUID companyId;

    private String planCode;

    private SubscriptionStatus status = SubscriptionStatus.ACTIVE;

    private Instant startedAt;

    private Instant currentPeriodEnd;

    private boolean autoRenew = true;

    public UUID getCompanyId() { return companyId; }
    public void setCompanyId(UUID companyId) { this.companyId = companyId; }
    public String getPlanCode() { return planCode; }
    public void setPlanCode(String planCode) { this.planCode = planCode; }
    public SubscriptionStatus getStatus() { return status; }
    public void setStatus(SubscriptionStatus status) { this.status = status; }
    public Instant getStartedAt() { return startedAt; }
    public void setStartedAt(Instant startedAt) { this.startedAt = startedAt; }
    public Instant getCurrentPeriodEnd() { return currentPeriodEnd; }
    public void setCurrentPeriodEnd(Instant currentPeriodEnd) { this.currentPeriodEnd = currentPeriodEnd; }
    public boolean isAutoRenew() { return autoRenew; }
    public void setAutoRenew(boolean autoRenew) { this.autoRenew = autoRenew; }
}
