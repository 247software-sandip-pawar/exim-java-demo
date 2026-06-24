package com.eximplatform.billing;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Billing microservice: subscription plans, company subscriptions, and platform payments — the
 * platform's own revenue (owns exim_billing). Companies are validated against the identity service
 * over REST; plans are seeded reference data.
 *
 * <p>Integration with a billing provider (card charging, invoicing webhooks) is deferred (Phase 6):
 * a platform payment records an {@code externalRef} placeholder and is marked paid via the API.
 */
@SpringBootApplication(scanBasePackages = "com.eximplatform")
public class BillingServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(BillingServiceApplication.class, args);
    }
}
