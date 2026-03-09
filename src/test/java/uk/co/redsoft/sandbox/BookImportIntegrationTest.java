package uk.co.redsoft.sandbox;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.containers.RabbitMQContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import uk.co.redsoft.sandbox.adapters.out.persistence.JpaBookRepository;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
class BookImportIntegrationTest {

    @Container
    @ServiceConnection
    static MySQLContainer<?> mysql = new MySQLContainer<>("mysql:8.4");

    @Container
    @ServiceConnection
    static RabbitMQContainer rabbitmq = new RabbitMQContainer("rabbitmq:4-management");

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JpaBookRepository jpaBookRepository;

    @Test
    void importCsvPublishesToRabbitMqAndPersistsToDatabase() throws Exception {
        var csv = """
                title,author,isbn,genre
                Clean Code,Robert C. Martin,978-0132350884,Software Engineering
                Domain-Driven Design,Eric Evans,978-0321125217,Software Architecture
                """;
        var file = new MockMultipartFile("file", "books.csv", "text/csv", csv.getBytes());

        mockMvc.perform(multipart("/books/import").file(file))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.enqueued").value(2))
                .andExpect(jsonPath("$.skipped").value(0));

        await().atMost(10, SECONDS).untilAsserted(() ->
                assertThat(jpaBookRepository.findAll()).hasSizeGreaterThanOrEqualTo(2)
        );

        assertThat(jpaBookRepository.existsByIsbn("978-0132350884")).isTrue();
        assertThat(jpaBookRepository.existsByIsbn("978-0321125217")).isTrue();
    }
}
