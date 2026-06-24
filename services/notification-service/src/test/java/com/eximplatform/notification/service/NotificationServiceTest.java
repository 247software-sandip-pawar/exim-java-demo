package com.eximplatform.notification.service;

import com.eximplatform.common.client.IdentityClient;
import com.eximplatform.common.exception.NotFoundException;
import com.eximplatform.notification.domain.Notification;
import com.eximplatform.notification.domain.NotificationType;
import com.eximplatform.notification.dto.NotificationRequest;
import com.eximplatform.notification.dto.NotificationResponse;
import com.eximplatform.notification.repository.NotificationRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @Mock NotificationRepository notificationRepository;
    @Mock IdentityClient identityClient;

    @InjectMocks NotificationService notificationService;

    private NotificationRequest request(UUID recipient) {
        NotificationRequest req = new NotificationRequest();
        req.setRecipientCompanyId(recipient);
        req.setType(NotificationType.ORDER_CREATED);
        req.setTitle("Order created");
        req.setBody("Your order ORD-123 has been created.");
        return req;
    }

    @Test
    void create_persistsUnreadNotificationWhenRecipientValid() {
        UUID recipient = UUID.randomUUID();
        when(identityClient.companyExists(recipient)).thenReturn(true);
        when(notificationRepository.save(any(Notification.class))).thenAnswer(inv -> inv.getArgument(0));

        NotificationResponse response = notificationService.create(request(recipient));

        assertThat(response.getType()).isEqualTo("ORDER_CREATED");
        assertThat(response.isRead()).isFalse();
        assertThat(response.getChannel()).isEqualTo("IN_APP");
    }

    @Test
    void create_failsWhenRecipientMissing() {
        UUID recipient = UUID.randomUUID();
        when(identityClient.companyExists(recipient)).thenReturn(false);

        assertThatThrownBy(() -> notificationService.create(request(recipient)))
                .isInstanceOf(NotFoundException.class);

        verify(notificationRepository, never()).save(any());
    }

    @Test
    void markRead_setsReadTrue() {
        UUID id = UUID.randomUUID();
        Notification n = new Notification();
        n.setId(id);
        n.setRead(false);
        when(notificationRepository.findById(id)).thenReturn(Optional.of(n));
        when(notificationRepository.save(any(Notification.class))).thenAnswer(inv -> inv.getArgument(0));

        NotificationResponse response = notificationService.markRead(id);

        assertThat(response.isRead()).isTrue();
    }

    @Test
    void get_throwsWhenMissing() {
        UUID id = UUID.randomUUID();
        when(notificationRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> notificationService.get(id))
                .isInstanceOf(NotFoundException.class);
    }
}
