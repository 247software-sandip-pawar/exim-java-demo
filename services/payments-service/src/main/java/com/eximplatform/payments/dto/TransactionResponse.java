package com.eximplatform.payments.dto;

import com.eximplatform.payments.domain.Transaction;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public class TransactionResponse {

    private UUID id;
    private UUID orderId;
    private UUID payerCompanyId;
    private UUID payeeCompanyId;
    private BigDecimal amount;
    private String currency;
    private String method;
    private String paymentTermCode;
    private String status;
    private String externalRef;
    private Instant createdAt;
    private Instant updatedAt;

    public static TransactionResponse from(Transaction t) {
        TransactionResponse out = new TransactionResponse();
        out.id = t.getId();
        out.orderId = t.getOrderId();
        out.payerCompanyId = t.getPayerCompanyId();
        out.payeeCompanyId = t.getPayeeCompanyId();
        out.amount = t.getAmount();
        out.currency = t.getCurrency();
        out.method = t.getMethod() != null ? t.getMethod().name() : null;
        out.paymentTermCode = t.getPaymentTermCode();
        out.status = t.getStatus() != null ? t.getStatus().name() : null;
        out.externalRef = t.getExternalRef();
        out.createdAt = t.getCreatedAt();
        out.updatedAt = t.getUpdatedAt();
        return out;
    }

    public UUID getId() { return id; }
    public UUID getOrderId() { return orderId; }
    public UUID getPayerCompanyId() { return payerCompanyId; }
    public UUID getPayeeCompanyId() { return payeeCompanyId; }
    public BigDecimal getAmount() { return amount; }
    public String getCurrency() { return currency; }
    public String getMethod() { return method; }
    public String getPaymentTermCode() { return paymentTermCode; }
    public String getStatus() { return status; }
    public String getExternalRef() { return externalRef; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
}
