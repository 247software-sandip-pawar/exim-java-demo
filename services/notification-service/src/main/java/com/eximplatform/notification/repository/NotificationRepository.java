package com.eximplatform.notification.repository;

import com.eximplatform.notification.domain.Notification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.UUID;

public interface NotificationRepository extends MongoRepository<Notification, UUID> {
    Page<Notification> findByRecipientCompanyId(UUID recipientCompanyId, Pageable pageable);
    Page<Notification> findByRecipientCompanyIdAndRead(UUID recipientCompanyId, boolean read, Pageable pageable);
}
