package uk.co.redsoft.sandbox.adapters.out.http;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.MountableFile;
import org.wiremock.integrations.testcontainers.WireMockContainer;
import org.springframework.web.client.RestClient;
import uk.co.redsoft.sandbox.domain.model.BookPrice;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@Testcontainers
class PriceCatalogueRestClientAdapterTest {

    @Container
    static WireMockContainer wiremock = new WireMockContainer("wiremock/wiremock:3.10.0")
            .withCopyFileToContainer(
                    MountableFile.forClasspathResource("wiremock/mappings"),
                    "/home/wiremock/mappings/"
            );

    private PriceCatalogueRestClientAdapter adapter;

    @BeforeEach
    void setUp() {
        var restClient = RestClient.builder()
                .baseUrl(wiremock.getBaseUrl())
                .defaultHeaders(headers -> headers.setBasicAuth("catalogue-user", "secret"))
                .build();
        adapter = new PriceCatalogueRestClientAdapter(restClient);
    }

    @Test
    void returnsEmptyListWhenIsbnNotFound() {
        var prices = adapter.fetchPrices("978-9999999999");

        assertThat(prices).isEmpty();
    }

    @Test
    void throwsPriceCatalogueExceptionOnServerError() {
        assertThatThrownBy(() -> adapter.fetchPrices("978-5555555555"))
                .isInstanceOf(PriceCatalogueException.class);
    }

    @Test
    void returnsEmptyListWhenPricesFieldIsAbsent() {
        var prices = adapter.fetchPrices("978-4444444444");

        assertThat(prices).isEmpty();
    }

    @Test
    void returnsEmptyListWhenResponseBodyIsNull() {
        var prices = adapter.fetchPrices("978-3333333333");

        assertThat(prices).isEmpty();
    }

    @Test
    void fetchesPricesAndMapsToDomainModel() {
        var isbn = "978-0132350884";

        var prices = adapter.fetchPrices(isbn);

        assertThat(prices).hasSize(5);
        assertThat(prices).extracting(BookPrice::countryCode)
                .containsExactlyInAnyOrder("GBR", "USA", "FRA", "DEU", "AUS");
        assertThat(prices).allMatch(p -> p.isbn().equals(isbn));
        var gbr = prices.stream().filter(p -> "GBR".equals(p.countryCode())).findFirst().orElseThrow();
        assertThat(gbr.price()).isEqualByComparingTo(new BigDecimal("24.99"));
    }
}
