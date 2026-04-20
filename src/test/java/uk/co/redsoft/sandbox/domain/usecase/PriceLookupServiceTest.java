package uk.co.redsoft.sandbox.domain.usecase;

import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.redsoft.sandbox.domain.model.BookPrice;
import uk.co.redsoft.sandbox.domain.ports.out.PriceCataloguePort;
import uk.co.redsoft.sandbox.domain.ports.out.PriceRepositoryPort;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PriceLookupServiceTest {

    @Mock
    private PriceCataloguePort priceCataloguePort;

    @Mock
    private PriceRepositoryPort priceStore;

    @Spy
    private SimpleMeterRegistry meterRegistry = new SimpleMeterRegistry();

    @InjectMocks
    private PriceLookupService priceLookupService;

    @Test
    void lookupDeletesExistingAndStoresNewPrices() {
        var isbn = "978-0132350884";
        var prices = List.of(
                new BookPrice(isbn, "GBR", new BigDecimal("24.99")),
                new BookPrice(isbn, "USA", new BigDecimal("34.99"))
        );
        when(priceCataloguePort.fetchPrices(isbn)).thenReturn(prices);

        priceLookupService.lookupAndStorePrices(isbn);

        var order = inOrder(priceStore);
        order.verify(priceStore).deleteByIsbn(isbn);
        order.verify(priceStore).saveAll(prices);
    }

    @Test
    void doesNotDeleteOrSaveWhenCatalogueReturnsEmptyList() {
        var isbn = "978-0132350884";
        when(priceCataloguePort.fetchPrices(isbn)).thenReturn(List.of());

        priceLookupService.lookupAndStorePrices(isbn);

        verify(priceStore, never()).deleteByIsbn(any());
        verify(priceStore, never()).saveAll(any());
    }

    @Test
    void replacesExistingPricesWhenCalledAgainForSameIsbn() {
        var isbn = "978-0132350884";
        var firstPrices = List.of(new BookPrice(isbn, "GBR", new BigDecimal("24.99")));
        var secondPrices = List.of(
                new BookPrice(isbn, "GBR", new BigDecimal("26.99")),
                new BookPrice(isbn, "EUR", new BigDecimal("28.99"))
        );
        when(priceCataloguePort.fetchPrices(isbn))
                .thenReturn(firstPrices)
                .thenReturn(secondPrices);

        priceLookupService.lookupAndStorePrices(isbn);
        priceLookupService.lookupAndStorePrices(isbn);

        var order = inOrder(priceStore);
        order.verify(priceStore).deleteByIsbn(isbn);
        order.verify(priceStore).saveAll(firstPrices);
        order.verify(priceStore).deleteByIsbn(isbn);
        order.verify(priceStore).saveAll(secondPrices);
    }
}
