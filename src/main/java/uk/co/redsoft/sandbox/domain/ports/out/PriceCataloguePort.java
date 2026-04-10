package uk.co.redsoft.sandbox.domain.ports.out;

import uk.co.redsoft.sandbox.domain.model.BookPrice;

import java.util.List;

public interface PriceCataloguePort {

    List<BookPrice> fetchPrices(String isbn);
}
