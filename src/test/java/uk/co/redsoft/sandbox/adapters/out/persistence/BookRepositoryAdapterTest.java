package uk.co.redsoft.sandbox.adapters.out.persistence;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.redsoft.sandbox.domain.model.Book;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BookRepositoryAdapterTest {

    @Mock
    private JpaBookRepository jpaBookRepository;

    @InjectMocks
    private BookRepositoryAdapter adapter;

    @Test
    void saveMapsBookToEntityAndBack() {
        var book = new Book(null, "Clean Code", "Robert C. Martin", "978-0132350884", "Software Engineering");
        when(jpaBookRepository.save(any(BookEntity.class))).thenReturn(
                new BookEntity(1L, "Clean Code", "Robert C. Martin", "978-0132350884", "Software Engineering")
        );

        var result = adapter.save(book);

        assertThat(result.id()).isEqualTo(1L);
        assertThat(result.title()).isEqualTo("Clean Code");
        assertThat(result.author()).isEqualTo("Robert C. Martin");
        assertThat(result.isbn()).isEqualTo("978-0132350884");
        assertThat(result.genre()).isEqualTo("Software Engineering");
    }

    @Test
    void saveWithIdMapsEntityWithId() {
        var book = new Book(5L, "Updated Title", "Author", "978-0000000000", "Tech");
        when(jpaBookRepository.save(any(BookEntity.class))).thenReturn(
                new BookEntity(5L, "Updated Title", "Author", "978-0000000000", "Tech")
        );

        var result = adapter.save(book);

        assertThat(result.id()).isEqualTo(5L);
        assertThat(result.title()).isEqualTo("Updated Title");
    }

    @Test
    void findAllMapsEntitiesToBooks() {
        when(jpaBookRepository.findAll()).thenReturn(List.of(
                new BookEntity(1L, "Book One", "Author A", "978-0000000001", "Fiction"),
                new BookEntity(2L, "Book Two", "Author B", "978-0000000002", "Science")
        ));

        var result = adapter.findAll();

        assertThat(result).hasSize(2);
        assertThat(result.get(0).title()).isEqualTo("Book One");
        assertThat(result.get(1).title()).isEqualTo("Book Two");
    }

    @Test
    void findByIdReturnsBookWhenFound() {
        when(jpaBookRepository.findById(1L)).thenReturn(
                Optional.of(new BookEntity(1L, "Clean Code", "Author", "978-0000000000", "Tech"))
        );

        var result = adapter.findById(1L);

        assertThat(result).isPresent();
        assertThat(result.get().title()).isEqualTo("Clean Code");
    }

    @Test
    void findByIdReturnsEmptyWhenNotFound() {
        when(jpaBookRepository.findById(99L)).thenReturn(Optional.empty());

        assertThat(adapter.findById(99L)).isEmpty();
    }

    @Test
    void existsByIsbnDelegatesToRepository() {
        when(jpaBookRepository.existsByIsbn("978-0000000000")).thenReturn(true);

        assertThat(adapter.existsByIsbn("978-0000000000")).isTrue();
    }

    @Test
    void deleteByIdDelegatesToRepository() {
        adapter.deleteById(1L);

        verify(jpaBookRepository).deleteById(1L);
    }
}
