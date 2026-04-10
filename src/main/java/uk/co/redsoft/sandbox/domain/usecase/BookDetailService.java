package uk.co.redsoft.sandbox.domain.usecase;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.co.redsoft.sandbox.domain.model.BookDetail;
import uk.co.redsoft.sandbox.domain.model.BookPrice;
import uk.co.redsoft.sandbox.domain.ports.in.BookDetailUseCase;
import uk.co.redsoft.sandbox.domain.ports.out.BookStore;
import uk.co.redsoft.sandbox.domain.ports.out.PriceStore;

import java.util.Optional;

@RequiredArgsConstructor
@Service
public class BookDetailService implements BookDetailUseCase {

    private static final String DEFAULT_COUNTRY_CODE = "GBR";

    private final BookStore bookStore;
    private final PriceStore priceStore;

    @Override
    public Optional<BookDetail> getBookDetail(Long id) {
        return bookStore.findById(id).map(book -> {
            var price = priceStore.findByIsbn(book.isbn()).stream()
                    .filter(p -> DEFAULT_COUNTRY_CODE.equals(p.countryCode()))
                    .map(BookPrice::price)
                    .findFirst()
                    .orElse(null);
            return new BookDetail(book.id(), book.title(), book.author(),
                    book.isbn(), book.genre(), price);
        });
    }
}
