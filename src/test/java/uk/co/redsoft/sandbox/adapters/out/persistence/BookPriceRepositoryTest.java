package uk.co.redsoft.sandbox.adapters.out.persistence;

import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Testcontainers
class BookPriceRepositoryTest {

    @Container
    @ServiceConnection
    static MySQLContainer<?> mysql = new MySQLContainer<>("mysql:8.4");

    @Autowired
    private JpaBookPriceRepository jpaBookPriceRepository;

    @Autowired
    private EntityManager entityManager;

    @Test
    void savesAllPricesForIsbn() {
        var isbn = "978-0132350884";
        jpaBookPriceRepository.save(new BookPriceEntity(isbn, "GBR", new BigDecimal("24.99")));
        jpaBookPriceRepository.save(new BookPriceEntity(isbn, "USA", new BigDecimal("34.99")));
        jpaBookPriceRepository.save(new BookPriceEntity(isbn, "FRA", new BigDecimal("29.95")));

        var prices = jpaBookPriceRepository.findByIsbn(isbn);

        assertThat(prices).hasSize(3);
        assertThat(prices).extracting(BookPriceEntity::getCountryCode)
                .containsExactlyInAnyOrder("GBR", "USA", "FRA");
    }

    @Test
    void findByIsbnReturnsEmptyListForUnknownIsbn() {
        var prices = jpaBookPriceRepository.findByIsbn("978-9999999999");

        assertThat(prices).isEmpty();
    }

    @Test
    void duplicateIsbnAndCountryViolatesPrimaryKeyConstraint() {
        var isbn = "978-0135957059";
        entityManager.persist(new BookPriceEntity(isbn, "GBR", new BigDecimal("29.99")));
        entityManager.flush();

        assertThatThrownBy(() -> {
            entityManager.persist(new BookPriceEntity(isbn, "GBR", new BigDecimal("31.00")));
            entityManager.flush();
        }).isInstanceOf(Exception.class);
    }
}
