package uk.co.redsoft.sandbox.adapters.out.persistence;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.redsoft.sandbox.domain.model.BookPrice;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BookPriceRepositoryAdapterTest {

    @Mock
    private JpaBookPriceRepository jpaBookPriceRepository;

    @InjectMocks
    private BookPriceRepositoryAdapter adapter;

    @Test
    void saveAllMapsDomainPricesToEntitiesAndPersists() {
        var isbn = "978-0132350884";
        var prices = List.of(
                new BookPrice(isbn, "GBR", new BigDecimal("24.99")),
                new BookPrice(isbn, "USA", new BigDecimal("34.99"))
        );

        adapter.saveAll(prices);

        verify(jpaBookPriceRepository).saveAll(anyList());
    }

    @Test
    void findByIsbnMapsEntitiesToDomainPrices() {
        var isbn = "978-0132350884";
        when(jpaBookPriceRepository.findByIsbn(isbn)).thenReturn(List.of(
                new BookPriceEntity(isbn, "GBR", new BigDecimal("24.99")),
                new BookPriceEntity(isbn, "USA", new BigDecimal("34.99"))
        ));

        var result = adapter.findByIsbn(isbn);

        assertThat(result).hasSize(2);
        assertThat(result).extracting(BookPrice::countryCode)
                .containsExactlyInAnyOrder("GBR", "USA");
        assertThat(result).extracting(BookPrice::isbn)
                .containsOnly(isbn);
    }

    @Test
    void deleteByIsbnDelegatesToRepository() {
        var isbn = "978-0132350884";

        adapter.deleteByIsbn(isbn);

        verify(jpaBookPriceRepository).deleteByIsbn(eq(isbn));
    }
}
