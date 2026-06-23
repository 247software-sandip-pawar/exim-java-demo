package com.eximplatform.messaging.dto;

import com.eximplatform.messaging.domain.Conversation;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public class ConversationResponse {

    private UUID id;
    private String subject;
    private UUID rfqId;
    private List<UUID> participantCompanyIds;
    private String status;
    private Instant createdAt;
    private Instant updatedAt;

    public static ConversationResponse from(Conversation c) {
        ConversationResponse out = new ConversationResponse();
        out.id = c.getId();
        out.subject = c.getSubject();
        out.rfqId = c.getRfqId();
        out.participantCompanyIds = c.getParticipantCompanyIds();
        out.status = c.getStatus() != null ? c.getStatus().name() : null;
        out.createdAt = c.getCreatedAt();
        out.updatedAt = c.getUpdatedAt();
        return out;
    }

    public UUID getId() { return id; }
    public String getSubject() { return subject; }
    public UUID getRfqId() { return rfqId; }
    public List<UUID> getParticipantCompanyIds() { return participantCompanyIds; }
    public String getStatus() { return status; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
}
