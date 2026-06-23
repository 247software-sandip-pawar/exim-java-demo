package com.eximplatform.messaging.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

/** Post a message to a conversation, optionally carrying an in-chat offer. */
public class MessageRequest {

    @NotNull
    private UUID senderCompanyId;

    private UUID senderUserId;

    @NotBlank
    private String body;

    private OfferDto offer;

    public UUID getSenderCompanyId() { return senderCompanyId; }
    public void setSenderCompanyId(UUID senderCompanyId) { this.senderCompanyId = senderCompanyId; }
    public UUID getSenderUserId() { return senderUserId; }
    public void setSenderUserId(UUID senderUserId) { this.senderUserId = senderUserId; }
    public String getBody() { return body; }
    public void setBody(String body) { this.body = body; }
    public OfferDto getOffer() { return offer; }
    public void setOffer(OfferDto offer) { this.offer = offer; }
}
