package uk.co.redsoft.sandbox.adapters.out.http.stub;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.client.ClientHttpRequest;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.stereotype.Component;
import uk.co.redsoft.sandbox.domain.ports.out.PriceCatalogueStubRepositoryPort;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

@RequiredArgsConstructor
@Component
public class StubPriceCatalogueClientHttpRequestFactory implements ClientHttpRequestFactory {

    private final PriceCatalogueStubRepositoryPort stubRepositoryPort;

    @Override
    public ClientHttpRequest createRequest(URI uri, HttpMethod httpMethod) {
        String[] segments = uri.getPath().split("/");
        // path is /catalogue/books/{isbn}/prices — isbn is second-to-last segment
        var isbn = segments[segments.length - 2];

        return stubRepositoryPort.findByIsbn(isbn)
                .map(r -> new StubClientHttpRequest(uri, httpMethod, HttpStatus.valueOf(r.httpStatus()), r.responseBody()))
                .orElseGet(() -> new StubClientHttpRequest(uri, httpMethod, HttpStatus.NOT_FOUND, ""));
    }

    private static class StubClientHttpRequest implements ClientHttpRequest {

        private final URI uri;
        private final HttpMethod method;
        private final HttpStatus status;
        private final String responseBody;

        StubClientHttpRequest(URI uri, HttpMethod method, HttpStatus status, String responseBody) {
            this.uri = uri;
            this.method = method;
            this.status = status;
            this.responseBody = responseBody;
        }

        @Override
        public ClientHttpResponse execute() {
            return new StubClientHttpResponse(status, responseBody);
        }

        @Override
        public HttpMethod getMethod() { return method; }

        @Override
        public URI getURI() { return uri; }

        @Override
        public HttpHeaders getHeaders() { return new HttpHeaders(); }

        @Override
        public Map<String, Object> getAttributes() { return new HashMap<>(); }

        @Override
        public OutputStream getBody() { return OutputStream.nullOutputStream(); }
    }

    private static class StubClientHttpResponse implements ClientHttpResponse {

        private final HttpStatus status;
        private final String body;
        private final HttpHeaders headers;

        StubClientHttpResponse(HttpStatus status, String body) {
            this.status = status;
            this.body = body;
            this.headers = new HttpHeaders();
            this.headers.setContentType(MediaType.APPLICATION_JSON);
        }

        @Override
        public HttpStatusCode getStatusCode() { return status; }

        @Override
        public String getStatusText() { return status.getReasonPhrase(); }

        @Override
        public HttpHeaders getHeaders() { return headers; }

        @Override
        public InputStream getBody() {
            return new ByteArrayInputStream(body.getBytes(StandardCharsets.UTF_8));
        }

        @Override
        public void close() {}
    }
}
