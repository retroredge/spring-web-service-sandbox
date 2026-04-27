package uk.co.redsoft.sandbox;

import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.utility.MountableFile;
import org.wiremock.integrations.testcontainers.WireMockContainer;

public abstract class AbstractWireMockContainersIntegrationTest extends AbstractContainersIntegrationTest {

    static final WireMockContainer wiremock = new WireMockContainer("wiremock/wiremock:3.10.0")
            .withCopyFileToContainer(
                    MountableFile.forClasspathResource("wiremock/mappings"),
                    "/home/wiremock/mappings/"
            );

    static {
        wiremock.start();
    }

    @DynamicPropertySource
    static void wireMockProperties(DynamicPropertyRegistry registry) {
        registry.add("pricing.catalogue.base-url", wiremock::getBaseUrl);
    }
}
