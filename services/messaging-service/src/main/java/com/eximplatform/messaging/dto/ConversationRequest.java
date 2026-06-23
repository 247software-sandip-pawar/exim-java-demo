package com.eximplatform.messaging.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;
import java.util.UUID;

/** Open a conversation between companies, optionally anchored to an RFQ. */
public class ConversationRequest {

    @NotBlank
    private String subject;

    private UUID rfqId;

    @NotEmpty(message = "at least one participant company is required")
    private List<UUID> participantCompanyIds;

    public String getSubject() { return subject; }
    public void setSubject(String subject) { this.subject = subject; }
    public UUID getRfqId() { return rfqId; }
    public void setRfqId(UUID rfqId) { this.rfqId = rfqId; }
    public List<UUID> getParticipantCompanyIds() { return participantCompanyIds; }
    public void setParticipantCompanyIds(List<UUID> participantCompanyIds) { this.participantCompanyIds = participantCompanyIds; }
}
