package uk.co.redsoft.sandbox.adapters.out.http.stub;

import org.springframework.http.HttpMethod;
import org.springframework.http.client.ClientHttpRequest;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URI;
import java.util.concurrent.atomic.AtomicBoolean;

public class ToggleableClientHttpRequestFactory implements ClientHttpRequestFactory {

    private final ClientHttpRequestFactory real;
    private final ClientHttpRequestFactory stub;
    private final AtomicBoolean stubEnabled;

    public ToggleableClientHttpRequestFactory(ClientHttpRequestFactory real,
                                              ClientHttpRequestFactory stub,
                                              boolean initiallyEnabled) {
        this.real = real;
        this.stub = stub;
        this.stubEnabled = new AtomicBoolean(initiallyEnabled);
    }

    public boolean isStubEnabled() {
        return stubEnabled.get();
    }

    public void setStubEnabled(boolean enabled) {
        stubEnabled.set(enabled);
    }

    @Override
    public ClientHttpRequest createRequest(URI uri, HttpMethod httpMethod) throws IOException {
        return stubEnabled.get()
                ? stub.createRequest(uri, httpMethod)
                : real.createRequest(uri, httpMethod);
    }
}
