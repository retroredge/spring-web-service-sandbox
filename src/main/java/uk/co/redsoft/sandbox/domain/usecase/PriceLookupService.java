package uk.co.redsoft.sandbox.domain.usecase;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import uk.co.redsoft.sandbox.domain.ports.in.PriceLookupUseCase;
import uk.co.redsoft.sandbox.domain.ports.out.PriceCataloguePort;
import uk.co.redsoft.sandbox.domain.ports.out.PriceStore;

@Slf4j
@RequiredArgsConstructor
@Service
public class PriceLookupService implements PriceLookupUseCase {

    private final PriceCataloguePort priceCataloguePort;
    private final PriceStore priceStore;

    @Override
    public void lookupAndStorePrices(String isbn) {
        log.debug("Fetching prices for ISBN: {}", isbn);
        var prices = priceCataloguePort.fetchPrices(isbn);
        if (prices.isEmpty()) {
            log.warn("No prices returned for ISBN: {}", isbn);
            return;
        }
        try {
            priceStore.saveAll(prices);
            log.debug("Stored {} price(s) for ISBN: {}", prices.size(), isbn);
        } catch (DataIntegrityViolationException e) {
            log.warn("Prices already exist for ISBN: {}, skipping", isbn);
        }
    }
}
