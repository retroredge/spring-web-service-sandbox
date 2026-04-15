package uk.co.redsoft.sandbox.domain.usecase;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.redsoft.sandbox.domain.model.BookPrice;
import uk.co.redsoft.sandbox.domain.ports.out.PriceCataloguePort;
import uk.co.redsoft.sandbox.domain.ports.out.PriceRepositoryPort;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PriceLookupServiceTest {

    @Mock
    private PriceCataloguePort priceCataloguePort;

    @Mock
    private PriceRepositoryPort priceStore;

    @InjectMocks
    private PriceLookupService priceLookupService;

    @Test
    void lookupAndStoresPricesWhenCatalogueReturnsPrices() {
        var isbn = "978-0132350884";
        var prices = List.of(
                new BookPrice(isbn, "GBR", new BigDecimal("24.99")),
                new BookPrice(isbn, "USA", new BigDecimal("34.99"))
        );
        when(priceCataloguePort.fetchPrices(isbn)).thenReturn(prices);

        priceLookupService.lookupAndStorePrices(isbn);

        verify(priceStore).saveAll(prices);
    }

    @Test
    void doesNotSaveWhenCatalogueReturnsEmptyList() {
        var isbn = "978-0132350884";
        when(priceCataloguePort.fetchPrices(isbn)).thenReturn(List.of());

        priceLookupService.lookupAndStorePrices(isbn);

        verify(priceStore, never()).saveAll(List.of());
    }
}
