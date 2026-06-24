package com.eximplatform.trust;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Trust microservice: company ratings and dispute resolution (owns exim_trust). Companies are
 * validated against the identity service over REST; ratings reference the order they relate to by
 * bare UUID.
 */
@SpringBootApplication(scanBasePackages = "com.eximplatform")
public class TrustServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(TrustServiceApplication.class, args);
    }
}
