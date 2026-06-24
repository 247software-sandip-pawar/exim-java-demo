package com.eximplatform.payments;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Payments microservice: escrow transactions, letters of credit, and payment terms (owns
 * exim_payments). The order being paid for is read from the orders service over REST.
 *
 * <p>Integration with a licensed payment/escrow partner (and its signature-verified webhooks) is
 * deferred (Phase 5): money movement is modelled as a guarded state machine here, with an
 * {@code externalRef} placeholder where the partner reference would live. Optimistic locking
 * (the {@code @Version} field) guards concurrent updates to a transaction.
 */
@SpringBootApplication(scanBasePackages = "com.eximplatform")
public class PaymentsServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(PaymentsServiceApplication.class, args);
    }
}
