package uk.co.redsoft.sandbox.adapters.out.http;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.test.context.TestPropertySource;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.containers.RabbitMQContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import uk.co.redsoft.sandbox.domain.model.BookPrice;
import uk.co.redsoft.sandbox.domain.ports.out.PriceCataloguePort;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Testcontainers
@AutoConfigureWireMock(port = 0, files = "classpath:/wiremock")
@TestPropertySource(properties = "pricing.catalogue.base-url=http://localhost:${wiremock.server.port}")
class PriceCatalogueRestClientAdapterIntegrationTest {

    @Container
    @ServiceConnection
    static MySQLContainer<?> mysql = new MySQLContainer<>("mysql:8.4");

    @Container
    @ServiceConnection
    static RabbitMQContainer rabbitmq = new RabbitMQContainer("rabbitmq:4-alpine");

    @Autowired
    private PriceCataloguePort priceCataloguePort;

    @Test
    void fetchPricesReturnsMappedPricesForKnownIsbn() {
        var isbn = "978-0132350884";

        var prices = priceCataloguePort.fetchPrices(isbn);

        assertThat(prices).hasSize(5);
        assertThat(prices).extracting(BookPrice::countryCode)
                .containsExactlyInAnyOrder("GBR", "USA", "FRA", "DEU", "AUS");
        var gbr = prices.stream().filter(p -> "GBR".equals(p.countryCode())).findFirst().orElseThrow();
        assertThat(gbr.price()).isEqualByComparingTo(new BigDecimal("24.99"));
    }

    @Test
    void fetchPricesReturnsEmptyListForUnknownIsbn() {
        var prices = priceCataloguePort.fetchPrices("978-9999999999");

        assertThat(prices).isEmpty();
    }
}
