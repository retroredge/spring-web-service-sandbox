package uk.co.redsoft.sandbox.adapters.out.http;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;

import java.util.List;

@HttpExchange("/catalogue/books")
public interface PriceCatalogueClient {

    @GetExchange("/{isbn}/prices")
    List<BookPriceResponse> getPrices(@PathVariable String isbn);
}
