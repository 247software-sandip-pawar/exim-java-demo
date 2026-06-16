package com.eximplatform.gateway;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * Boots the full gateway context (including Spring Cloud Gateway autoconfiguration and the route
 * definitions in application.yml). Fails fast if the gateway starter or route config is misconfigured.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class GatewayApplicationTests {

    @Test
    void contextLoads() {
    }
}
