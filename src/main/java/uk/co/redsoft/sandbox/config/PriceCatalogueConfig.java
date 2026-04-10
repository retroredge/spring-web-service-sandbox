package uk.co.redsoft.sandbox.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class PriceCatalogueConfig {

    @Bean
    RestClient priceCatalogueRestClient(
            @Value("${pricing.catalogue.base-url}") String baseUrl,
            @Value("${pricing.catalogue.username}") String username,
            @Value("${pricing.catalogue.password}") String password) {
        return RestClient.builder()
                .baseUrl(baseUrl)
                .defaultHeaders(h -> h.setBasicAuth(username, password))
                .build();
    }
}
