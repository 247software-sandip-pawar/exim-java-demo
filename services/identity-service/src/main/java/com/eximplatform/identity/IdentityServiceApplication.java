package com.eximplatform.identity;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Identity microservice: authentication (register/login), users, and companies.
 * Scans the whole {@code com.eximplatform} base package so the shared common/security
 * libraries on the classpath are picked up.
 */
@SpringBootApplication(scanBasePackages = "com.eximplatform")
public class IdentityServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(IdentityServiceApplication.class, args);
    }
}
