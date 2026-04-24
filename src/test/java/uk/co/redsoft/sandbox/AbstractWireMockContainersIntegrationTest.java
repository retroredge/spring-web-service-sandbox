package uk.co.redsoft.sandbox;

import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.utility.MountableFile;
import org.wiremock.integrations.testcontainers.WireMockContainer;

public abstract class AbstractWireMockContainersIntegrationTest extends AbstractContainersIntegrationTest {

    @Container
    static WireMockContainer wiremock = new WireMockContainer("wiremock/wiremock:3.10.0")
            .withCopyFileToContainer(
                    MountableFile.forClasspathResource("wiremock/mappings"),
                    "/home/wiremock/mappings/"
            );

    @DynamicPropertySource
    static void wireMockProperties(DynamicPropertyRegistry registry) {
        registry.add("pricing.catalogue.base-url", wiremock::getBaseUrl);
    }
}
