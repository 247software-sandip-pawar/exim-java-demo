package com.eximplatform.logistics;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Logistics microservice: books and tracks shipments for orders (owns exim_logistics). The order is
 * read from the orders service over REST; logistics partners are seeded reference data.
 *
 * <p>Real carrier/3PL API integration and live tracking webhooks are deferred (Phase 5): tracking
 * events are recorded via the API, not pulled from a carrier feed.
 */
@SpringBootApplication(scanBasePackages = "com.eximplatform")
public class LogisticsServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(LogisticsServiceApplication.class, args);
    }
}
