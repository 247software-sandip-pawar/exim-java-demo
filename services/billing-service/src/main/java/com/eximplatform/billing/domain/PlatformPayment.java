package com.eximplatform.billing.domain;

import com.eximplatform.common.domain.BaseEntity;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * A payment a company makes to the platform for a subscription period. {@code externalRef} is a
 * placeholder for the billing provider's charge reference (integration deferred).
 */
@Document(collection = "platform_payments")
public class PlatformPayment extends BaseEntity {

    @Indexed
    private UUID subscriptionId;

    @Indexed
    private UUID companyId;

    private BigDecimal amount;

    private String currency;

    private PaymentStatus status = PaymentStatus.PENDING;

    private String externalRef;

    public UUID getSubscriptionId() { return subscriptionId; }
    public void setSubscriptionId(UUID subscriptionId) { this.subscriptionId = subscriptionId; }
    public UUID getCompanyId() { return companyId; }
    public void setCompanyId(UUID companyId) { this.companyId = companyId; }
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }
    public PaymentStatus getStatus() { return status; }
    public void setStatus(PaymentStatus status) { this.status = status; }
    public String getExternalRef() { return externalRef; }
    public void setExternalRef(String externalRef) { this.externalRef = externalRef; }
}
