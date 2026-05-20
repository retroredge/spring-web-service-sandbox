package uk.co.redsoft.sandbox.adapters.out.http.stub;

import org.junit.jupiter.api.Test;
import org.springframework.http.client.ClientHttpRequestFactory;
import uk.co.redsoft.sandbox.domain.ports.out.PriceCatalogueStubRepositoryPort;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class PriceCatalogueStubEndpointTest {

    private final ToggleableClientHttpRequestFactory requestFactory =
            new ToggleableClientHttpRequestFactory(
                    mock(ClientHttpRequestFactory.class),
                    new StubPriceCatalogueClientHttpRequestFactory(mock(PriceCatalogueStubRepositoryPort.class)),
                    false);

    private final PriceCatalogueStubEndpoint endpoint =
            new PriceCatalogueStubEndpoint(requestFactory);

    @Test
    void getState_returnsCurrentStubEnabledFalse() {
        assertThat(endpoint.getState()).containsEntry("stubEnabled", false);
    }

    @Test
    void setStubEnabled_true_updatesFactory() {
        endpoint.setStubEnabled(true);

        assertThat(requestFactory.isStubEnabled()).isTrue();
        assertThat(endpoint.getState()).containsEntry("stubEnabled", true);
    }

    @Test
    void setStubEnabled_false_updatesFactory() {
        requestFactory.setStubEnabled(true);

        endpoint.setStubEnabled(false);

        assertThat(requestFactory.isStubEnabled()).isFalse();
        assertThat(endpoint.getState()).containsEntry("stubEnabled", false);
    }
}
