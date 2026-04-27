package uk.co.redsoft.sandbox.adapters.out.persistence;

import io.micrometer.core.annotation.Timed;
import io.micrometer.core.instrument.DistributionSummary;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import uk.co.redsoft.sandbox.domain.model.BookPrice;
import uk.co.redsoft.sandbox.domain.ports.out.PriceRepositoryPort;

import java.util.List;

@RequiredArgsConstructor
@Component
public class BookPriceRepositoryAdapter implements PriceRepositoryPort {

    private final JpaBookPriceRepository jpaBookPriceRepository;
    private final MeterRegistry meterRegistry;

    @Timed("books.pricing.db.save")
    @Override
    public void saveAll(List<BookPrice> prices) {
        var entities = prices.stream()
                .map(p -> new BookPriceEntity(p.isbn(), p.countryCode(), p.price()))
                .toList();
        jpaBookPriceRepository.saveAll(entities);
        DistributionSummary.builder("books.pricing.prices.per.book")
                .register(meterRegistry)
                .record(prices.size());
        meterRegistry.counter("books.pricing.lookup.outcome", "result", "success").increment();
    }

    @Override
    @Transactional
    public void deleteByIsbn(String isbn) {
        jpaBookPriceRepository.deleteByIsbn(isbn);
    }

    @Override
    public List<BookPrice> findByIsbn(String isbn) {
        return jpaBookPriceRepository.findByIsbn(isbn).stream()
                .map(e -> new BookPrice(e.getIsbn(), e.getCountryCode(), e.getPrice()))
                .toList();
    }
}
