package uk.co.redsoft.sandbox.config;

import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManagerBuilder;
import org.apache.hc.client5.http.ssl.ClientTlsStrategyBuilder;
import org.apache.hc.client5.http.ssl.NoopHostnameVerifier;
import org.apache.hc.core5.ssl.SSLContexts;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;
import uk.co.redsoft.sandbox.adapters.out.http.stub.StubPriceCatalogueClientHttpRequestFactory;
import uk.co.redsoft.sandbox.adapters.out.http.stub.ToggleableClientHttpRequestFactory;

@Configuration
public class PriceCatalogueConfig {

    @Bean
    ToggleableClientHttpRequestFactory priceCatalogueRequestFactory(
            @Value("${pricing.catalogue.stub-enabled:false}") boolean stubEnabled,
            @Value("${pricing.catalogue.skip-ssl:false}") boolean skipSsl,
            StubPriceCatalogueClientHttpRequestFactory stubFactory) throws Exception {
        return new ToggleableClientHttpRequestFactory(
                buildRealFactory(skipSsl),
                stubFactory,
                stubEnabled);
    }

    @Bean
    RestClient priceCatalogueRestClient(
            @Value("${pricing.catalogue.base-url}") String baseUrl,
            @Value("${pricing.catalogue.username}") String username,
            @Value("${pricing.catalogue.password}") String password,
            ToggleableClientHttpRequestFactory requestFactory) {
        return RestClient.builder()
                .baseUrl(baseUrl)
                .defaultHeaders(h -> h.setBasicAuth(username, password))
                .requestFactory(requestFactory)
                .build();
    }

    private ClientHttpRequestFactory buildRealFactory(boolean skipSsl) throws Exception {
        if (!skipSsl) {
            return new SimpleClientHttpRequestFactory();
        }
        var sslContext = SSLContexts.custom()
                .loadTrustMaterial((chain, authType) -> true)
                .build();
        var tlsStrategy = ClientTlsStrategyBuilder.create()
                .setSslContext(sslContext)
                .setHostnameVerifier(NoopHostnameVerifier.INSTANCE)
                .buildClassic();
        var connectionManager = PoolingHttpClientConnectionManagerBuilder.create()
                .setTlsSocketStrategy(tlsStrategy)
                .build();
        var httpClient = HttpClients.custom()
                .setConnectionManager(connectionManager)
                .build();
        return new HttpComponentsClientHttpRequestFactory(httpClient);
    }
}
