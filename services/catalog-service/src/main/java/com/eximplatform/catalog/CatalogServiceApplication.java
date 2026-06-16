package com.eximplatform.catalog;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Catalog microservice: products and HS codes (owns exim_catalog). Validates seller companies
 * against the identity service over REST. Scans {@code com.eximplatform} for the shared libraries.
 */
@SpringBootApplication(scanBasePackages = "com.eximplatform")
public class CatalogServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(CatalogServiceApplication.class, args);
    }
}
