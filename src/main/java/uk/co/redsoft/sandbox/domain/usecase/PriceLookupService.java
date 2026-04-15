package uk.co.redsoft.sandbox.domain.usecase;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.co.redsoft.sandbox.domain.ports.in.PriceLookupUseCase;
import uk.co.redsoft.sandbox.domain.ports.out.PriceCataloguePort;
import uk.co.redsoft.sandbox.domain.ports.out.PriceRepositoryPort;

@Slf4j
@RequiredArgsConstructor
@Service
public class PriceLookupService implements PriceLookupUseCase {

    private final PriceCataloguePort priceCataloguePort;
    private final PriceRepositoryPort priceStore;

    @Transactional
    @Override
    public void lookupAndStorePrices(String isbn) {
        log.debug("Fetching prices for ISBN: {}", isbn);
        var prices = priceCataloguePort.fetchPrices(isbn);
        if (prices.isEmpty()) {
            log.warn("No prices returned for ISBN: {}, skipping update", isbn);
            return;
        }
        priceStore.deleteByIsbn(isbn);
        priceStore.saveAll(prices);
        log.debug("Stored {} price(s) for ISBN: {}", prices.size(), isbn);
    }
}
