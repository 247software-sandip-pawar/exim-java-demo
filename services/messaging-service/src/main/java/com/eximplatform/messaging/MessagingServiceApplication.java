package com.eximplatform.messaging;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Messaging microservice: buyer/seller conversations and messages, optionally carrying in-chat
 * offers (owns exim_messaging). Participant companies are validated against the identity service
 * over REST.
 *
 * <p>Real-time delivery over a WebSocket {@code /ws} (and Redis pub/sub for fan-out) is deferred
 * (Phase 4); today messages are exchanged over plain REST and read back per conversation.
 */
@SpringBootApplication(scanBasePackages = "com.eximplatform")
public class MessagingServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(MessagingServiceApplication.class, args);
    }
}
