package com.eximplatform.verification;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Verification microservice: company KYC documents. Validates companies against the identity
 * service over REST (no shared database). Scans {@code com.eximplatform} for the shared libraries.
 */
@SpringBootApplication(scanBasePackages = "com.eximplatform")
public class VerificationServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(VerificationServiceApplication.class, args);
    }
}
