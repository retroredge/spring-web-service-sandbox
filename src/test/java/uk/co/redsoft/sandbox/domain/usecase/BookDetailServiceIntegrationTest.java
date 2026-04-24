package uk.co.redsoft.sandbox.domain.usecase;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import uk.co.redsoft.sandbox.AbstractContainersIntegrationTest;
import uk.co.redsoft.sandbox.adapters.out.persistence.BookEntity;
import uk.co.redsoft.sandbox.adapters.out.persistence.BookPriceEntity;
import uk.co.redsoft.sandbox.adapters.out.persistence.JpaBookPriceRepository;
import uk.co.redsoft.sandbox.adapters.out.persistence.JpaBookRepository;
import uk.co.redsoft.sandbox.domain.ports.in.BookDetailUseCase;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class BookDetailServiceIntegrationTest extends AbstractContainersIntegrationTest {

    @Autowired
    private BookDetailUseCase bookDetailService;

    @Autowired
    private JpaBookRepository jpaBookRepository;

    @Autowired
    private JpaBookPriceRepository jpaBookPriceRepository;

    @Test
    void getBookDetailReturnsBookWithGbrPrice() {
        var isbn = "978-0132350884";
        var saved = jpaBookRepository.save(new BookEntity("Clean Code", "Robert C. Martin", isbn, "Software Engineering"));
        jpaBookPriceRepository.save(new BookPriceEntity(isbn, "GBR", new BigDecimal("24.99")));
        jpaBookPriceRepository.save(new BookPriceEntity(isbn, "USA", new BigDecimal("34.99")));

        var result = bookDetailService.getBookDetail(saved.getId());

        assertThat(result).isPresent();
        assertThat(result.get().title()).isEqualTo("Clean Code");
        assertThat(result.get().gbrPrice()).isEqualByComparingTo(new BigDecimal("24.99"));
    }

    @Test
    void getBookDetailReturnsNullPriceWhenNoPriceExists() {
        var saved = jpaBookRepository.save(new BookEntity("The Pragmatic Programmer", "David Thomas", "978-0135957059", "Software Engineering"));

        var result = bookDetailService.getBookDetail(saved.getId());

        assertThat(result).isPresent();
        assertThat(result.get().gbrPrice()).isNull();
    }

    @Test
    void getBookDetailReturnsEmptyWhenBookNotFound() {
        var result = bookDetailService.getBookDetail(Long.MAX_VALUE);

        assertThat(result).isEmpty();
    }
}
