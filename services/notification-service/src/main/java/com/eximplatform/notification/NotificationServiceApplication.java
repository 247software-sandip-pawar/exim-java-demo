package com.eximplatform.notification;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Notification microservice: in-app notifications for companies/users (owns exim_notification).
 * Recipients are validated against the identity service over REST.
 *
 * <p>Push delivery (FCM for mobile, email) is deferred (Phase 6): notifications are stored and read
 * back over REST (channel {@code IN_APP}); other services create them by calling this API.
 */
@SpringBootApplication(scanBasePackages = "com.eximplatform")
public class NotificationServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(NotificationServiceApplication.class, args);
    }
}
