package uk.co.redsoft.sandbox.adapters.out.http.stub;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpMethod;
import org.springframework.http.client.ClientHttpRequest;
import org.springframework.http.client.ClientHttpRequestFactory;

import java.io.IOException;
import java.net.URI;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ToggleableClientHttpRequestFactoryTest {

    @Mock
    private ClientHttpRequestFactory real;

    @Mock
    private ClientHttpRequestFactory stub;

    @Test
    void whenStubDisabled_delegatesToRealFactory() throws IOException {
        var factory = new ToggleableClientHttpRequestFactory(real, stub, false);
        var uri = URI.create("http://example.com/catalogue/books/123/prices");
        var expected = mock(ClientHttpRequest.class);
        when(real.createRequest(uri, HttpMethod.GET)).thenReturn(expected);

        var result = factory.createRequest(uri, HttpMethod.GET);

        assertThat(result).isSameAs(expected);
        verifyNoInteractions(stub);
    }

    @Test
    void whenStubEnabled_delegatesToStubFactory() throws IOException {
        var factory = new ToggleableClientHttpRequestFactory(real, stub, true);
        var uri = URI.create("http://example.com/catalogue/books/123/prices");
        var expected = mock(ClientHttpRequest.class);
        when(stub.createRequest(uri, HttpMethod.GET)).thenReturn(expected);

        var result = factory.createRequest(uri, HttpMethod.GET);

        assertThat(result).isSameAs(expected);
        verifyNoInteractions(real);
    }

    @Test
    void setStubEnabled_true_switchesToStub() throws IOException {
        var factory = new ToggleableClientHttpRequestFactory(real, stub, false);
        var uri = URI.create("http://example.com/catalogue/books/123/prices");

        factory.setStubEnabled(true);
        factory.createRequest(uri, HttpMethod.GET);

        verify(stub).createRequest(uri, HttpMethod.GET);
        verifyNoInteractions(real);
    }

    @Test
    void setStubEnabled_false_switchesToReal() throws IOException {
        var factory = new ToggleableClientHttpRequestFactory(real, stub, true);
        var uri = URI.create("http://example.com/catalogue/books/123/prices");

        factory.setStubEnabled(false);
        factory.createRequest(uri, HttpMethod.GET);

        verify(real).createRequest(uri, HttpMethod.GET);
        verifyNoInteractions(stub);
    }

    @Test
    void isStubEnabled_reflectsCurrentState() {
        var factory = new ToggleableClientHttpRequestFactory(real, stub, false);

        assertThat(factory.isStubEnabled()).isFalse();

        factory.setStubEnabled(true);
        assertThat(factory.isStubEnabled()).isTrue();
    }
}
