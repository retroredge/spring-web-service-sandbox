package uk.co.redsoft.sandbox.config;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PriceCatalogueConfigTest {

    private final PriceCatalogueConfig config = new PriceCatalogueConfig();

    @Test
    void buildsRestClientWithSslVerificationEnabled() throws Exception {
        var client = config.priceCatalogueRestClient(
                "http://localhost:8081", "user", "secret", false);

        assertThat(client).isNotNull();
    }

    @Test
    void buildsRestClientWithSslVerificationSkipped() throws Exception {
        var client = config.priceCatalogueRestClient(
                "https://localhost:8443", "user", "secret", true);

        assertThat(client).isNotNull();
    }
}
