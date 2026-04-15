package uk.co.redsoft.sandbox.adapters.out.http;

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
            return List.of();
        }

        if (response == null || response.prices() == null) {
            return List.of();
        }

        return response.prices().stream()
                .map(r -> new BookPrice(isbn, r.countryCode(), r.price()))
                .toList();
    }
}
