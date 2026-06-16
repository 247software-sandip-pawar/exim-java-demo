package com.eximplatform.sourcing;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Sourcing microservice: RFQs and matching (owns exim_sourcing). Validates buyer companies via the
 * identity service and matches RFQs to products via the catalog service, both over REST.
 */
@SpringBootApplication(scanBasePackages = "com.eximplatform")
public class SourcingServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(SourcingServiceApplication.class, args);
    }
}
