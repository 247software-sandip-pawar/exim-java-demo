package com.eximplatform.messaging.domain;

import com.eximplatform.common.domain.BaseEntity;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * A conversation thread between companies, optionally anchored to an RFQ. {@code participantCompanyIds}
 * link to companies in the identity service by bare UUID (validated over REST on create). Messages
 * live in their own collection, keyed by {@code conversationId}.
 */
@Document(collection = "conversations")
public class Conversation extends BaseEntity {

    private String subject;

    private UUID rfqId;

    @Indexed
    private List<UUID> participantCompanyIds = new ArrayList<>();

    private ConversationStatus status = ConversationStatus.OPEN;

    public String getSubject() { return subject; }
    public void setSubject(String subject) { this.subject = subject; }
    public UUID getRfqId() { return rfqId; }
    public void setRfqId(UUID rfqId) { this.rfqId = rfqId; }
    public List<UUID> getParticipantCompanyIds() { return participantCompanyIds; }
    public void setParticipantCompanyIds(List<UUID> participantCompanyIds) { this.participantCompanyIds = participantCompanyIds; }
    public ConversationStatus getStatus() { return status; }
    public void setStatus(ConversationStatus status) { this.status = status; }
}
