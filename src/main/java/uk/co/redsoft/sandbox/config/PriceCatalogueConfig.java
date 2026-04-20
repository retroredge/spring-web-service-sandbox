package uk.co.redsoft.sandbox.config;

import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManagerBuilder;
import org.apache.hc.client5.http.ssl.ClientTlsStrategyBuilder;
import org.apache.hc.client5.http.ssl.NoopHostnameVerifier;
import org.apache.hc.core5.ssl.SSLContexts;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

@Configuration
public class PriceCatalogueConfig {

    @Bean
    RestClient priceCatalogueRestClient(
            @Value("${pricing.catalogue.base-url}") String baseUrl,
            @Value("${pricing.catalogue.username}") String username,
            @Value("${pricing.catalogue.password}") String password,
            @Value("${pricing.catalogue.skip-ssl:false}") boolean skipSsl) throws Exception {

        var builder = RestClient.builder()
                .baseUrl(baseUrl)
                .defaultHeaders(h -> h.setBasicAuth(username, password));

        if (skipSsl) {
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
            builder.requestFactory(new HttpComponentsClientHttpRequestFactory(httpClient));
        }

        return builder.build();
    }
}
