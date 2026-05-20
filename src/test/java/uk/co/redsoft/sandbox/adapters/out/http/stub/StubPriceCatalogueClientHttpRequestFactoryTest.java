package uk.co.redsoft.sandbox.adapters.out.http.stub;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import uk.co.redsoft.sandbox.domain.model.PriceCatalogueStubResponse;
import uk.co.redsoft.sandbox.domain.ports.out.PriceCatalogueStubRepositoryPort;

import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StubPriceCatalogueClientHttpRequestFactoryTest {

    @Mock
    private PriceCatalogueStubRepositoryPort stubRepositoryPort;

    @InjectMocks
    private StubPriceCatalogueClientHttpRequestFactory factory;

    @Test
    void knownIsbn_returnsConfiguredStatusAndBody() throws IOException {
        var isbn = "978-0132350884";
        var body = "{\"status\":\"ok\",\"prices\":[]}";
        when(stubRepositoryPort.findByIsbn(isbn))
                .thenReturn(Optional.of(new PriceCatalogueStubResponse(isbn, 200, body)));
        var uri = URI.create("http://stub/catalogue/books/" + isbn + "/prices");

        var response = factory.createRequest(uri, HttpMethod.GET).execute();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(new String(response.getBody().readAllBytes(), StandardCharsets.UTF_8)).isEqualTo(body);
    }

    @Test
    void unknownIsbn_returns404WithEmptyBody() throws IOException {
        when(stubRepositoryPort.findByIsbn("978-0000000000")).thenReturn(Optional.empty());
        var uri = URI.create("http://stub/catalogue/books/978-0000000000/prices");

        var response = factory.createRequest(uri, HttpMethod.GET).execute();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(new String(response.getBody().readAllBytes(), StandardCharsets.UTF_8)).isEmpty();
    }

    @Test
    void response_hasJsonContentType() throws IOException {
        var isbn = "978-0132350884";
        when(stubRepositoryPort.findByIsbn(isbn))
                .thenReturn(Optional.of(new PriceCatalogueStubResponse(isbn, 200, "{}")));
        var uri = URI.create("http://stub/catalogue/books/" + isbn + "/prices");

        var response = factory.createRequest(uri, HttpMethod.GET).execute();

        assertThat(response.getHeaders().getContentType()).isNotNull();
        assertThat(response.getHeaders().getContentType().toString()).contains("application/json");
    }

    @Test
    void customHttpStatus_isReturned() throws IOException {
        var isbn = "978-0132350884";
        when(stubRepositoryPort.findByIsbn(isbn))
                .thenReturn(Optional.of(new PriceCatalogueStubResponse(isbn, 503, "")));
        var uri = URI.create("http://stub/catalogue/books/" + isbn + "/prices");

        var response = factory.createRequest(uri, HttpMethod.GET).execute();

        assertThat(response.getStatusCode().value()).isEqualTo(503);
    }
}
