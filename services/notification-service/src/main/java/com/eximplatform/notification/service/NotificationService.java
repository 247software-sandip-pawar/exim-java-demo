package com.eximplatform.notification.service;

import com.eximplatform.common.api.PageResponse;
import com.eximplatform.common.client.IdentityClient;
import com.eximplatform.common.exception.NotFoundException;
import com.eximplatform.notification.domain.Notification;
import com.eximplatform.notification.dto.NotificationRequest;
import com.eximplatform.notification.dto.NotificationResponse;
import com.eximplatform.notification.repository.NotificationRepository;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * In-app notifications. The recipient company is validated against the identity service over REST.
 * Other services create notifications by calling this API; recipients list and mark them read.
 * MongoDB has no dirty-checking, so marking-read saves explicitly.
 */
@Service
@Transactional(transactionManager = "notificationTransactionManager")
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final IdentityClient identityClient;

    public NotificationService(NotificationRepository notificationRepository, IdentityClient identityClient) {
        this.notificationRepository = notificationRepository;
        this.identityClient = identityClient;
    }

    public NotificationResponse create(NotificationRequest req) {
        if (!identityClient.companyExists(req.getRecipientCompanyId())) {
            throw new NotFoundException("Recipient company not found.");
        }
        Notification n = new Notification();
        n.setRecipientCompanyId(req.getRecipientCompanyId());
        n.setRecipientUserId(req.getRecipientUserId());
        n.setType(req.getType());
        n.setTitle(req.getTitle());
        n.setBody(req.getBody());
        n.setRead(false);
        n.setChannel("IN_APP");
        return NotificationResponse.from(notificationRepository.save(n));
    }

    @Transactional(transactionManager = "notificationTransactionManager", readOnly = true)
    public NotificationResponse get(UUID id) {
        return NotificationResponse.from(findOrThrow(id));
    }

    @Transactional(transactionManager = "notificationTransactionManager", readOnly = true)
    public PageResponse<NotificationResponse> list(UUID recipientCompanyId, Boolean unread, Pageable pageable) {
        var page = recipientCompanyId != null && Boolean.TRUE.equals(unread)
                ? notificationRepository.findByRecipientCompanyIdAndRead(recipientCompanyId, false, pageable)
                : recipientCompanyId != null
                ? notificationRepository.findByRecipientCompanyId(recipientCompanyId, pageable)
                : notificationRepository.findAll(pageable);
        return PageResponse.from(page, NotificationResponse::from);
    }

    public NotificationResponse markRead(UUID id) {
        Notification n = findOrThrow(id);
        n.setRead(true);
        return NotificationResponse.from(notificationRepository.save(n));
    }

    private Notification findOrThrow(UUID id) {
        return notificationRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Notification not found."));
    }
}
