package uk.co.redsoft.sandbox;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.containers.RabbitMQContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.MountableFile;
import org.wiremock.integrations.testcontainers.WireMockContainer;
import uk.co.redsoft.sandbox.adapters.out.persistence.JpaBookPriceRepository;
import uk.co.redsoft.sandbox.domain.model.CreateBookCommand;
import uk.co.redsoft.sandbox.domain.ports.in.BookUseCase;

import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

@SpringBootTest
@Testcontainers
class SandboxApplicationTest {

    @Container
    @ServiceConnection
    static MySQLContainer<?> mysql = new MySQLContainer<>("mysql:8.4");

    @Container
    @ServiceConnection
    static RabbitMQContainer rabbitmq = new RabbitMQContainer("rabbitmq:4-alpine");

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

    @Autowired
    private BookUseCase bookUseCase;

    @Autowired
    private JpaBookPriceRepository jpaBookPriceRepository;

    @Test
    void contextLoads() {
    }

    @Test
    void createBookTriggersAsyncPricingAndStoresPrices() {
        bookUseCase.create(new CreateBookCommand(
                "Clean Code", "Robert C. Martin", "978-0132350884", "Software Engineering"));

        await().atMost(10, TimeUnit.SECONDS).untilAsserted(() -> {
            var prices = jpaBookPriceRepository.findByIsbn("978-0132350884");
            assertThat(prices).hasSize(5);
        });
    }
}
