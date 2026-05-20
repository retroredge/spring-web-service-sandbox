package uk.co.redsoft.sandbox.adapters.out.persistence;

import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Testcontainers
class JpaPriceCatalogueStubRepositoryIntegrationTest {

    @Container
    @ServiceConnection
    static MySQLContainer<?> mysql = new MySQLContainer<>("mysql:8.4");

    @Autowired
    private JpaPriceCatalogueStubRepository repository;

    @Autowired
    private EntityManager entityManager;

    @Test
    void savedEntityHasDbGeneratedCreatedAt() {
        var saved = repository.save(new PriceCatalogueStubResponseEntity("978-0132350884", 200, "{\"status\":\"ok\"}"));

        var found = repository.findById(saved.getId());

        assertThat(found).isPresent();
        assertThat(found.get().getCreatedAt()).isNotNull();
    }

    @Test
    void findByIsbnReturnsEntityWhenPresent() {
        var isbn = "978-0132350884";
        repository.save(new PriceCatalogueStubResponseEntity(isbn, 200, "{\"status\":\"ok\"}"));

        var result = repository.findByIsbn(isbn);

        assertThat(result).isPresent();
        assertThat(result.get().getIsbn()).isEqualTo(isbn);
        assertThat(result.get().getHttpStatus()).isEqualTo(200);
        assertThat(result.get().getResponseBody()).isEqualTo("{\"status\":\"ok\"}");
    }

    @Test
    void findByIsbnReturnsEmptyWhenNotPresent() {
        var result = repository.findByIsbn("978-9999999999");

        assertThat(result).isEmpty();
    }

    @Test
    void duplicateIsbnViolatesUniqueConstraint() {
        var isbn = "978-0132350884";
        entityManager.persist(new PriceCatalogueStubResponseEntity(isbn, 200, "{}"));
        entityManager.flush();

        assertThatThrownBy(() -> {
            entityManager.persist(new PriceCatalogueStubResponseEntity(isbn, 404, ""));
            entityManager.flush();
        }).isInstanceOf(Exception.class);
    }
}
