package com.eximplatform.payments.dto;

import com.eximplatform.payments.domain.PaymentMethod;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

/** Initiate a payment (escrow) for an order. Amount &amp; parties are taken from the order itself. */
public class InitiatePaymentRequest {

    @NotNull
    private UUID orderId;

    @NotNull
    private PaymentMethod method;

    /** Optional payment-term code (must match a seeded term, e.g. NET_30). */
    private String paymentTermCode;

    public UUID getOrderId() { return orderId; }
    public void setOrderId(UUID orderId) { this.orderId = orderId; }
    public PaymentMethod getMethod() { return method; }
    public void setMethod(PaymentMethod method) { this.method = method; }
    public String getPaymentTermCode() { return paymentTermCode; }
    public void setPaymentTermCode(String paymentTermCode) { this.paymentTermCode = paymentTermCode; }
}
