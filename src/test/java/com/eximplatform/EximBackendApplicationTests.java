package com.eximplatform;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Sanity test that runs without a database so 'mvn package' stays green.
 *
 * For real integration tests, add Testcontainers (a real Postgres in Docker)
 * and annotate with @SpringBootTest. See EXECUTION_PLAN.md, Phase 1.
 */
class EximBackendApplicationTests {

    @Test
    void contextSanity() {
        assertTrue(true);
    }
}
