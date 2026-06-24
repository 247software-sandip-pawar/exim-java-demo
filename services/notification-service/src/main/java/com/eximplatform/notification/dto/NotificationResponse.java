package com.eximplatform.notification.dto;

import com.eximplatform.notification.domain.Notification;

import java.time.Instant;
import java.util.UUID;

public class NotificationResponse {

    private UUID id;
    private UUID recipientCompanyId;
    private UUID recipientUserId;
    private String type;
    private String title;
    private String body;
    private boolean read;
    private String channel;
    private Instant createdAt;

    public static NotificationResponse from(Notification n) {
        NotificationResponse out = new NotificationResponse();
        out.id = n.getId();
        out.recipientCompanyId = n.getRecipientCompanyId();
        out.recipientUserId = n.getRecipientUserId();
        out.type = n.getType() != null ? n.getType().name() : null;
        out.title = n.getTitle();
        out.body = n.getBody();
        out.read = n.isRead();
        out.channel = n.getChannel();
        out.createdAt = n.getCreatedAt();
        return out;
    }

    public UUID getId() { return id; }
    public UUID getRecipientCompanyId() { return recipientCompanyId; }
    public UUID getRecipientUserId() { return recipientUserId; }
    public String getType() { return type; }
    public String getTitle() { return title; }
    public String getBody() { return body; }
    public boolean isRead() { return read; }
    public String getChannel() { return channel; }
    public Instant getCreatedAt() { return createdAt; }
}
