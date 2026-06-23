package com.eximplatform.quotation;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Quotation microservice: a seller submits a quote against a buyer's RFQ; the buyer can accept,
 * reject, or counter it (owns exim_quotation). Buyer/seller companies are validated against the
 * identity service over REST. An accepted quote is what the orders service turns into an order.
 */
@SpringBootApplication(scanBasePackages = "com.eximplatform")
public class QuotationServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(QuotationServiceApplication.class, args);
    }
}
