package uk.co.redsoft.sandbox.adapters.out.http;

import io.micrometer.core.annotation.Timed;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import uk.co.redsoft.sandbox.domain.model.BookPrice;
import uk.co.redsoft.sandbox.domain.ports.out.PriceCataloguePort;

import java.util.List;

@RequiredArgsConstructor
@Component
public class PriceCatalogueRestClientAdapter implements PriceCataloguePort {

    private final RestClient restClient;
    private final MeterRegistry meterRegistry;

    @Timed("books.pricing.catalogue.fetch")
    @Override
    public List<BookPrice> fetchPrices(String isbn) {
        PriceCatalogueResponse response;
        try {
            response = restClient.get()
                    .uri("/catalogue/books/{isbn}/prices", isbn)
                    .retrieve()
                    .onStatus(status -> status.value() == 404, (request, resp) -> {
                        throw new BookPricesNotFoundException(isbn);
                    })
                    .onStatus(HttpStatusCode::isError, (request, resp) -> {
                        throw new PriceCatalogueException(resp.getStatusCode().value());
                    })
                    .body(PriceCatalogueResponse.class);
        } catch (BookPricesNotFoundException e) {
            meterRegistry.counter("books.pricing.catalogue.error", "type", "not_found").increment();
            return List.of();
        } catch (PriceCatalogueException e) {
            meterRegistry.counter("books.pricing.catalogue.error", "type", "server_error").increment();
            throw e;
        }

        if (response == null || response.prices() == null) {
            return List.of();
        }

        return response.prices().stream()
                .filter(this::isValid)
                .map(r -> new BookPrice(isbn, r.countryCode(), r.price()))
                .toList();
    }

    private boolean isValid(BookPriceResponse item) {
        return item != null
                && item.countryCode() != null && !item.countryCode().isBlank()
                && item.price() != null;
    }
}
