package com.eximplatform.admin;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Admin microservice: platform operations — KYC approval, sanctions screening, an audit log of admin
 * actions, and basic metrics (owns exim_admin). KYC decisions are applied to the verification service
 * over REST via {@code VerificationClient}; the sanctions list is seeded reference data.
 *
 * <p>Cross-service platform-wide metrics (counts pulled live from every service) and a real sanctions
 * data feed are deferred (Phase 6); metrics here are computed from admin-owned data.
 */
@SpringBootApplication(scanBasePackages = "com.eximplatform")
public class AdminServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(AdminServiceApplication.class, args);
    }
}
