package uk.co.redsoft.sandbox.domain.usecase;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.redsoft.sandbox.domain.model.Book;
import uk.co.redsoft.sandbox.domain.model.BookPrice;
import uk.co.redsoft.sandbox.domain.ports.out.BookStore;
import uk.co.redsoft.sandbox.domain.ports.out.PriceStore;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BookDetailServiceTest {

    @Mock
    private BookStore bookStore;

    @Mock
    private PriceStore priceStore;

    @InjectMocks
    private BookDetailService bookDetailService;

    @Test
    void returnsBookDetailWithPriceWhenGbrPriceExists() {
        var isbn = "978-0135957059";
        when(bookStore.findById(1L)).thenReturn(Optional.of(
                new Book(1L, "The Pragmatic Programmer", "David Thomas & Andrew Hunt", isbn, "Software Engineering")));
        when(priceStore.findByIsbn(isbn)).thenReturn(List.of(
                new BookPrice(isbn, "GBR", new BigDecimal("29.99")),
                new BookPrice(isbn, "USA", new BigDecimal("39.99"))
        ));

        var result = bookDetailService.getBookDetail(1L);

        assertThat(result).isPresent();
        assertThat(result.get().title()).isEqualTo("The Pragmatic Programmer");
        assertThat(result.get().gbrPrice()).isEqualByComparingTo("29.99");
    }

    @Test
    void returnsBookDetailWithNullPriceWhenNoGbrPriceExists() {
        var isbn = "978-0135957059";
        when(bookStore.findById(1L)).thenReturn(Optional.of(
                new Book(1L, "The Pragmatic Programmer", "David Thomas & Andrew Hunt", isbn, "Software Engineering")));
        when(priceStore.findByIsbn(isbn)).thenReturn(List.of(
                new BookPrice(isbn, "USA", new BigDecimal("39.99"))
        ));

        var result = bookDetailService.getBookDetail(1L);

        assertThat(result).isPresent();
        assertThat(result.get().gbrPrice()).isNull();
    }

    @Test
    void returnsEmptyWhenBookNotFound() {
        when(bookStore.findById(99L)).thenReturn(Optional.empty());

        assertThat(bookDetailService.getBookDetail(99L)).isEmpty();
    }
}
