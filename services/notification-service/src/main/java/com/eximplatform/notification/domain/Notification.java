package com.eximplatform.notification.domain;

import com.eximplatform.common.domain.BaseEntity;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.UUID;

/**
 * An in-app notification for a company (optionally a specific user). {@code recipientCompanyId} links
 * to the identity service by bare UUID. {@code channel} is IN_APP today; push channels are deferred.
 */
@Document(collection = "notifications")
public class Notification extends BaseEntity {

    @Indexed
    private UUID recipientCompanyId;

    private UUID recipientUserId;

    private NotificationType type;

    private String title;

    private String body;

    private boolean read = false;

    private String channel = "IN_APP";

    public UUID getRecipientCompanyId() { return recipientCompanyId; }
    public void setRecipientCompanyId(UUID recipientCompanyId) { this.recipientCompanyId = recipientCompanyId; }
    public UUID getRecipientUserId() { return recipientUserId; }
    public void setRecipientUserId(UUID recipientUserId) { this.recipientUserId = recipientUserId; }
    public NotificationType getType() { return type; }
    public void setType(NotificationType type) { this.type = type; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getBody() { return body; }
    public void setBody(String body) { this.body = body; }
    public boolean isRead() { return read; }
    public void setRead(boolean read) { this.read = read; }
    public String getChannel() { return channel; }
    public void setChannel(String channel) { this.channel = channel; }
}
