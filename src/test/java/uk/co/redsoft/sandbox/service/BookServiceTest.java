package uk.co.redsoft.sandbox.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.mock.web.MockMultipartFile;
import uk.co.redsoft.sandbox.config.RabbitConfig;
import uk.co.redsoft.sandbox.model.Book;
import uk.co.redsoft.sandbox.repository.BookEntity;
import uk.co.redsoft.sandbox.repository.BookRepository;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BookServiceTest {

    @Mock
    private BookRepository bookRepository;

    @Mock
    private AmqpTemplate amqpTemplate;

    @InjectMocks
    private BookService bookService;

    @Test
    void findAllReturnsAllBooks() {
        when(bookRepository.findAll()).thenReturn(List.of(
                entity(1L, "The Pragmatic Programmer"),
                entity(2L, "Clean Code")
        ));

        assertThat(bookService.findAll()).hasSize(2);
    }

    @Test
    void findByIdReturnsBookWhenFound() {
        when(bookRepository.findById(1L)).thenReturn(Optional.of(
                entity(1L, "The Pragmatic Programmer")
        ));

        Optional<Book> result = bookService.findById(1L);

        assertThat(result).isPresent();
        assertThat(result.get().title()).isEqualTo("The Pragmatic Programmer");
    }

    @Test
    void findByIdReturnsEmptyWhenNotFound() {
        when(bookRepository.findById(99L)).thenReturn(Optional.empty());

        assertThat(bookService.findById(99L)).isEmpty();
    }

    @Test
    void createSavesAndReturnsBook() {
        when(bookRepository.save(any(BookEntity.class))).thenReturn(entity(1L, "Clean Code"));

        var result = bookService.create(new Book(null, "Clean Code", "Author", "978-0000000000", "Software Engineering"));

        assertThat(result.id()).isEqualTo(1L);
        assertThat(result.title()).isEqualTo("Clean Code");
    }

    @Test
    void importCsvPublishesEachRowAsMessage() throws IOException {
        var csv = """
                title,author,isbn,genre
                Clean Code,Robert C. Martin,978-0132350884,Software Engineering
                The Pragmatic Programmer,David Thomas,978-0135957059,Software Engineering
                """;
        var file = new MockMultipartFile("file", "books-10.csv", "text/csv", csv.getBytes());

        bookService.importCsv(file);

        verify(amqpTemplate, times(2)).convertAndSend(eq(RabbitConfig.QUEUE), any(BookEntity.class));
    }

    private BookEntity entity(Long id, String title) {
        return new BookEntity(id, title, "Author", "978-0000000000", "Software Engineering");
    }
}
