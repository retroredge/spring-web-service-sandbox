package uk.co.redsoft.sandbox;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * Smoke test: verifies the full Spring application context loads successfully
 * with all infrastructure dependencies. Catches broken config before any
 * feature-level tests run.
 */
@SpringBootTest
class ApplicationContextIntegrationTest extends AbstractWireMockContainersIntegrationTest {

    @Test
    void applicationContextLoads() {
    }
}
