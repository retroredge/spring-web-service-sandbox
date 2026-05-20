package uk.co.redsoft.sandbox.config;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.redsoft.sandbox.adapters.out.http.stub.StubPriceCatalogueClientHttpRequestFactory;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class PriceCatalogueConfigTest {

    private final PriceCatalogueConfig config = new PriceCatalogueConfig();

    @Mock
    private StubPriceCatalogueClientHttpRequestFactory stubFactory;

    @Test
    void buildsRestClientWithSslVerificationEnabled() throws Exception {
        var factory = config.priceCatalogueRequestFactory(false, false, stubFactory);
        var client = config.priceCatalogueRestClient("http://localhost:8081", "user", "secret", factory);

        assertThat(client).isNotNull();
    }

    @Test
    void buildsRestClientWithSslVerificationSkipped() throws Exception {
        var factory = config.priceCatalogueRequestFactory(false, true, stubFactory);
        var client = config.priceCatalogueRestClient("https://localhost:8443", "user", "secret", factory);

        assertThat(client).isNotNull();
    }

    @Test
    void requestFactory_stubInitiallyDisabled_isNotStubEnabled() throws Exception {
        var factory = config.priceCatalogueRequestFactory(false, false, stubFactory);

        assertThat(factory.isStubEnabled()).isFalse();
    }

    @Test
    void requestFactory_stubInitiallyEnabled_isStubEnabled() throws Exception {
        var factory = config.priceCatalogueRequestFactory(true, false, stubFactory);

        assertThat(factory.isStubEnabled()).isTrue();
    }
}
