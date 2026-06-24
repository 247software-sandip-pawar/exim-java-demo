package com.eximplatform.trust.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

/** Raise a dispute against a company over an order. */
public class DisputeRequest {

    @NotNull
    private UUID orderId;

    @NotNull
    private UUID raisedByCompanyId;

    @NotNull
    private UUID againstCompanyId;

    @NotBlank
    private String reason;

    private String description;

    public UUID getOrderId() { return orderId; }
    public void setOrderId(UUID orderId) { this.orderId = orderId; }
    public UUID getRaisedByCompanyId() { return raisedByCompanyId; }
    public void setRaisedByCompanyId(UUID raisedByCompanyId) { this.raisedByCompanyId = raisedByCompanyId; }
    public UUID getAgainstCompanyId() { return againstCompanyId; }
    public void setAgainstCompanyId(UUID againstCompanyId) { this.againstCompanyId = againstCompanyId; }
    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}
