package uk.co.redsoft.sandbox.domain.ports.out;

import uk.co.redsoft.sandbox.domain.model.BookPrice;

import java.util.List;

public interface PriceStore {

    void saveAll(List<BookPrice> prices);

    List<BookPrice> findByIsbn(String isbn);
}
