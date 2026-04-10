package uk.co.redsoft.sandbox.domain.usecase;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.containers.RabbitMQContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import uk.co.redsoft.sandbox.adapters.out.persistence.BookEntity;
import uk.co.redsoft.sandbox.adapters.out.persistence.BookPriceEntity;
import uk.co.redsoft.sandbox.adapters.out.persistence.JpaBookPriceRepository;
import uk.co.redsoft.sandbox.adapters.out.persistence.JpaBookRepository;
import uk.co.redsoft.sandbox.domain.ports.in.BookDetailUseCase;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Testcontainers
class BookDetailServiceIntegrationTest {

    @Container
    @ServiceConnection
    static MySQLContainer<?> mysql = new MySQLContainer<>("mysql:8.4");

    @Container
    @ServiceConnection
    static RabbitMQContainer rabbitmq = new RabbitMQContainer("rabbitmq:4-alpine");

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
