package com.eximplatform.trust.domain;

import com.eximplatform.common.domain.BaseEntity;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.UUID;

/**
 * A dispute raised by one company against another over an order. Company ids and {@code orderId}
 * link to other services by bare UUID; {@code resolution} is filled in when the dispute is closed.
 */
@Document(collection = "disputes")
public class Dispute extends BaseEntity {

    @Indexed
    private UUID orderId;

    private UUID raisedByCompanyId;

    private UUID againstCompanyId;

    private String reason;

    private String description;

    private DisputeStatus status = DisputeStatus.OPEN;

    private String resolution;

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
    public DisputeStatus getStatus() { return status; }
    public void setStatus(DisputeStatus status) { this.status = status; }
    public String getResolution() { return resolution; }
    public void setResolution(String resolution) { this.resolution = resolution; }
}
