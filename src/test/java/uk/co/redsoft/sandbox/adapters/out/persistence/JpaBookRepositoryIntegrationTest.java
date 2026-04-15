package uk.co.redsoft.sandbox.adapters.out.persistence;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.containers.RabbitMQContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Testcontainers
class JpaBookRepositoryIntegrationTest {

    @Container
    @ServiceConnection
    static MySQLContainer<?> mysql = new MySQLContainer<>("mysql:8.4");

    @Container
    @ServiceConnection
    static RabbitMQContainer rabbitmq = new RabbitMQContainer("rabbitmq:4-alpine");

    @Autowired
    private JpaBookRepository jpaBookRepository;

    @Test
    void findAllReturnsAllSavedBooks() {
        jpaBookRepository.save(new BookEntity("Clean Code", "Robert C. Martin", "978-0132350884", "Software Engineering"));
        jpaBookRepository.save(new BookEntity("Designing Data-Intensive Applications", "Martin Kleppmann", "978-1449373320", "Software Engineering"));

        var books = jpaBookRepository.findAll();

        assertThat(books).hasSizeGreaterThanOrEqualTo(2);
    }

    @Test
    void savedBookHasDbGeneratedIdAndCreatedAt() {
        var saved = jpaBookRepository.save(
                new BookEntity("Clean Code", "Robert C. Martin", "978-0132350884", "Software Engineering"));

        var found = jpaBookRepository.findById(saved.getId());

        assertThat(found).isPresent();
        var book = found.get();
        assertThat(book.getId()).isNotNull();
        assertThat(book.getCreatedAt()).isNotNull();
        assertThat(book.getCreatedAt()).isBeforeOrEqualTo(Instant.now());
        assertThat(book.getTitle()).isEqualTo("Clean Code");
        assertThat(book.getAuthor()).isEqualTo("Robert C. Martin");
        assertThat(book.getIsbn()).isEqualTo("978-0132350884");
        assertThat(book.getGenre()).isEqualTo("Software Engineering");
    }
}
