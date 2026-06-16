package com.eximplatform.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * API gateway: the single public entry point. Routes requests to the backing microservices by
 * path (configured in application.yml). Token validation stays in each service — the gateway only
 * routes — so it remains thin and stateless.
 */
@SpringBootApplication
public class GatewayApplication {

    public static void main(String[] args) {
        SpringApplication.run(GatewayApplication.class, args);
    }
}
