package com.eximplatform.payments.domain;

import com.eximplatform.common.domain.BaseEntity;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * An escrow transaction for an order. {@code orderId} is unique so a given order has at most one
 * escrow transaction (the idempotency key for initiation); payer/payee company ids link to the
 * identity service by bare UUID. {@code externalRef} is a placeholder for the payment partner's
 * reference (integration deferred). Optimistic locking comes from {@link BaseEntity}'s version.
 */
@Document(collection = "transactions")
public class Transaction extends BaseEntity {

    @Indexed(unique = true)
    private UUID orderId;

    private UUID payerCompanyId;

    private UUID payeeCompanyId;

    private BigDecimal amount;

    private String currency;

    private PaymentMethod method;

    private String paymentTermCode;

    private TransactionStatus status = TransactionStatus.INITIATED;

    /** Reference from the external payment/escrow partner. Null until a partner is integrated. */
    private String externalRef;

    public UUID getOrderId() { return orderId; }
    public void setOrderId(UUID orderId) { this.orderId = orderId; }
    public UUID getPayerCompanyId() { return payerCompanyId; }
    public void setPayerCompanyId(UUID payerCompanyId) { this.payerCompanyId = payerCompanyId; }
    public UUID getPayeeCompanyId() { return payeeCompanyId; }
    public void setPayeeCompanyId(UUID payeeCompanyId) { this.payeeCompanyId = payeeCompanyId; }
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }
    public PaymentMethod getMethod() { return method; }
    public void setMethod(PaymentMethod method) { this.method = method; }
    public String getPaymentTermCode() { return paymentTermCode; }
    public void setPaymentTermCode(String paymentTermCode) { this.paymentTermCode = paymentTermCode; }
    public TransactionStatus getStatus() { return status; }
    public void setStatus(TransactionStatus status) { this.status = status; }
    public String getExternalRef() { return externalRef; }
    public void setExternalRef(String externalRef) { this.externalRef = externalRef; }
}
