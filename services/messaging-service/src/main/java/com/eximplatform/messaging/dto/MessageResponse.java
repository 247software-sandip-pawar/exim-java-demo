package com.eximplatform.messaging.dto;

import com.eximplatform.messaging.domain.Message;

import java.time.Instant;
import java.util.UUID;

public class MessageResponse {

    private UUID id;
    private UUID conversationId;
    private UUID senderCompanyId;
    private UUID senderUserId;
    private String body;
    private OfferDto offer;
    private Instant createdAt;

    public static MessageResponse from(Message m) {
        MessageResponse out = new MessageResponse();
        out.id = m.getId();
        out.conversationId = m.getConversationId();
        out.senderCompanyId = m.getSenderCompanyId();
        out.senderUserId = m.getSenderUserId();
        out.body = m.getBody();
        out.offer = OfferDto.from(m.getOffer());
        out.createdAt = m.getCreatedAt();
        return out;
    }

    public UUID getId() { return id; }
    public UUID getConversationId() { return conversationId; }
    public UUID getSenderCompanyId() { return senderCompanyId; }
    public UUID getSenderUserId() { return senderUserId; }
    public String getBody() { return body; }
    public OfferDto getOffer() { return offer; }
    public Instant getCreatedAt() { return createdAt; }
}
