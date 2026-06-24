package com.eximplatform.activity;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Activity microservice: a platform-wide audit trail of authenticated API calls (owns
 * exim_activity). The gateway records every authenticated request here (actor, method, path,
 * status); platform staff read it back filtered/paged. Writes are open to any authenticated
 * caller (so each user's own token authorizes recording their action); reads are staff-only.
 */
@SpringBootApplication(scanBasePackages = "com.eximplatform")
public class ActivityServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(ActivityServiceApplication.class, args);
    }
}
