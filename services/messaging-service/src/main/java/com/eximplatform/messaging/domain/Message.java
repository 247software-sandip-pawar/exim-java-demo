package com.eximplatform.messaging.domain;

import com.eximplatform.common.domain.BaseEntity;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.UUID;

/**
 * A message within a conversation. {@code conversationId} keys it to its {@link Conversation};
 * {@code senderCompanyId} must be a participant of that conversation. May carry an embedded
 * {@link Offer}.
 */
@Document(collection = "messages")
public class Message extends BaseEntity {

    @Indexed
    private UUID conversationId;

    private UUID senderCompanyId;

    private UUID senderUserId;

    private String body;

    private Offer offer;

    public UUID getConversationId() { return conversationId; }
    public void setConversationId(UUID conversationId) { this.conversationId = conversationId; }
    public UUID getSenderCompanyId() { return senderCompanyId; }
    public void setSenderCompanyId(UUID senderCompanyId) { this.senderCompanyId = senderCompanyId; }
    public UUID getSenderUserId() { return senderUserId; }
    public void setSenderUserId(UUID senderUserId) { this.senderUserId = senderUserId; }
    public String getBody() { return body; }
    public void setBody(String body) { this.body = body; }
    public Offer getOffer() { return offer; }
    public void setOffer(Offer offer) { this.offer = offer; }
}
