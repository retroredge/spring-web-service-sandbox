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
import uk.co.redsoft.sandbox.domain.model.CreateBookCommand;
import uk.co.redsoft.sandbox.domain.ports.in.BookDetailUseCase;
import uk.co.redsoft.sandbox.domain.ports.in.BookUseCase;

import java.math.BigDecimal;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

@SpringBootTest
@Testcontainers
class BookCreationPricingIntegrationTest {

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
    private BookDetailUseCase bookDetailUseCase;

    @Test
    void creatingBookTriggersAsyncPricingVisibleViaBookDetail() {
        var book = bookUseCase.create(new CreateBookCommand(
                "Domain-Driven Design", "Eric Evans", "978-0321125217", "Software Architecture"));

        await().atMost(10, TimeUnit.SECONDS).untilAsserted(() -> {
            var detail = bookDetailUseCase.getBookDetail(book.id());
            assertThat(detail).isPresent();
            assertThat(detail.get().gbrPrice()).isEqualByComparingTo(new BigDecimal("44.99"));
        });
    }
}
