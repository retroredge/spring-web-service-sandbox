package uk.co.redsoft.sandbox.adapters.out.persistence;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import uk.co.redsoft.sandbox.domain.model.BookPrice;
import uk.co.redsoft.sandbox.domain.ports.out.PriceStore;

import java.util.List;

@RequiredArgsConstructor
@Component
public class BookPriceRepositoryAdapter implements PriceStore {

    private final JpaBookPriceRepository jpaBookPriceRepository;

    @Override
    public void saveAll(List<BookPrice> prices) {
        var entities = prices.stream()
                .map(p -> new BookPriceEntity(p.isbn(), p.countryCode(), p.price()))
                .toList();
        jpaBookPriceRepository.saveAll(entities);
    }

    @Override
    public List<BookPrice> findByIsbn(String isbn) {
        return jpaBookPriceRepository.findByIdIsbn(isbn).stream()
                .map(e -> new BookPrice(e.getId().isbn(), e.getId().countryCode(), e.getPrice()))
                .toList();
    }
}
