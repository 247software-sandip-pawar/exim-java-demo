package com.eximplatform.trust.dto;

import com.eximplatform.trust.domain.Dispute;

import java.time.Instant;
import java.util.UUID;

public class DisputeResponse {

    private UUID id;
    private UUID orderId;
    private UUID raisedByCompanyId;
    private UUID againstCompanyId;
    private String reason;
    private String description;
    private String status;
    private String resolution;
    private Instant createdAt;
    private Instant updatedAt;

    public static DisputeResponse from(Dispute d) {
        DisputeResponse out = new DisputeResponse();
        out.id = d.getId();
        out.orderId = d.getOrderId();
        out.raisedByCompanyId = d.getRaisedByCompanyId();
        out.againstCompanyId = d.getAgainstCompanyId();
        out.reason = d.getReason();
        out.description = d.getDescription();
        out.status = d.getStatus() != null ? d.getStatus().name() : null;
        out.resolution = d.getResolution();
        out.createdAt = d.getCreatedAt();
        out.updatedAt = d.getUpdatedAt();
        return out;
    }

    public UUID getId() { return id; }
    public UUID getOrderId() { return orderId; }
    public UUID getRaisedByCompanyId() { return raisedByCompanyId; }
    public UUID getAgainstCompanyId() { return againstCompanyId; }
    public String getReason() { return reason; }
    public String getDescription() { return description; }
    public String getStatus() { return status; }
    public String getResolution() { return resolution; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
}
