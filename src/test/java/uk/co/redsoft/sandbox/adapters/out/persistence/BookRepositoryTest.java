package uk.co.redsoft.sandbox.adapters.out.persistence;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Testcontainers
class BookRepositoryTest {

    @Container
    @ServiceConnection
    static MySQLContainer<?> mysql = new MySQLContainer<>("mysql:8.4");

    @Autowired
    private JpaBookRepository jpaBookRepository;

    @Test
    void savesAndFindsBookById() {
        BookEntity saved = jpaBookRepository.save(
                new BookEntity("The Pragmatic Programmer", "David Thomas & Andrew Hunt", "978-0135957059", "Software Engineering")
        );

        Optional<BookEntity> found = jpaBookRepository.findById(saved.getId());

        assertThat(found).isPresent();
        assertThat(found.get().getTitle()).isEqualTo("The Pragmatic Programmer");
        assertThat(found.get().getCreatedAt()).isNotNull();
        assertThat(found.get().getCreatedAt()).isBeforeOrEqualTo(Instant.now());
    }

    @Test
    void findAllReturnsAllSavedBooks() {
        jpaBookRepository.save(new BookEntity("Clean Code", "Robert C. Martin", "978-0132350884", "Software Engineering"));
        jpaBookRepository.save(new BookEntity("Designing Data-Intensive Applications", "Martin Kleppmann", "978-1449373320", "Software Engineering"));

        List<BookEntity> books = jpaBookRepository.findAll();

        assertThat(books).hasSizeGreaterThanOrEqualTo(2);
    }
}
