package com.eximplatform.orders;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Orders microservice: turns an accepted quote into an order (owns exim_orders). The accepted quote
 * is read from the quotation service over REST; order creation is idempotent per quote. Documents
 * are generated against an order by the documents service.
 */
@SpringBootApplication(scanBasePackages = "com.eximplatform")
public class OrdersServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(OrdersServiceApplication.class, args);
    }
}
