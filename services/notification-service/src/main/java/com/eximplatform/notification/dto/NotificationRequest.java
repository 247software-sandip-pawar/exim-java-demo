package com.eximplatform.notification.dto;

import com.eximplatform.notification.domain.NotificationType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

/** Create a notification for a recipient company (and optionally a specific user). */
public class NotificationRequest {

    @NotNull
    private UUID recipientCompanyId;

    private UUID recipientUserId;

    @NotNull
    private NotificationType type;

    @NotBlank
    private String title;

    private String body;

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
}
