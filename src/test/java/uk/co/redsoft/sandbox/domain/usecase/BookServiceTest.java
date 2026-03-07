package uk.co.redsoft.sandbox.domain.usecase;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.redsoft.sandbox.domain.model.Book;
import uk.co.redsoft.sandbox.domain.ports.out.BookImportPort;
import uk.co.redsoft.sandbox.domain.ports.out.BookStore;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BookServiceTest {

    @Mock
    private BookStore bookStore;

    @Mock
    private BookImportPort bookImportPort;

    @InjectMocks
    private BookService bookService;

    @Test
    void findAllReturnsAllBooks() {
        when(bookStore.findAll()).thenReturn(List.of(
                new Book(1L, "The Pragmatic Programmer", "Author", "978-0000000000", "Software Engineering"),
                new Book(2L, "Clean Code", "Author", "978-0000000001", "Software Engineering")
        ));

        assertThat(bookService.findAll()).hasSize(2);
    }

    @Test
    void findByIdReturnsBookWhenFound() {
        when(bookStore.findById(1L)).thenReturn(Optional.of(
                new Book(1L, "The Pragmatic Programmer", "Author", "978-0000000000", "Software Engineering")
        ));

        Optional<Book> result = bookService.findById(1L);

        assertThat(result).isPresent();
        assertThat(result.get().title()).isEqualTo("The Pragmatic Programmer");
    }

    @Test
    void findByIdReturnsEmptyWhenNotFound() {
        when(bookStore.findById(99L)).thenReturn(Optional.empty());

        assertThat(bookService.findById(99L)).isEmpty();
    }

    @Test
    void createSavesAndReturnsBook() {
        when(bookStore.save(any(Book.class))).thenReturn(
                new Book(1L, "Clean Code", "Author", "978-0000000000", "Software Engineering"));

        var result = bookService.create(new Book(null, "Clean Code", "Author", "978-0000000000", "Software Engineering"));

        assertThat(result.id()).isEqualTo(1L);
        assertThat(result.title()).isEqualTo("Clean Code");
    }

    @Test
    void importBooksEnqueuesEachRow() throws IOException {
        var csv = """
                title,author,isbn,genre
                Clean Code,Robert C. Martin,978-0132350884,Software Engineering
                The Pragmatic Programmer,David Thomas,978-0135957059,Software Engineering
                """;

        bookService.importBooks(new ByteArrayInputStream(csv.getBytes()));

        verify(bookImportPort, times(2)).enqueue(any(Book.class));
    }
}
