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
    void saveMapsBookToEntityAndBackToDomain() {
        var book = new Book(null, "Clean Code", "Robert C. Martin", "978-0132350884", "Software Engineering");
        when(jpaBookRepository.save(any(BookEntity.class)))
                .thenReturn(new BookEntity(1L, "Clean Code", "Robert C. Martin", "978-0132350884", "Software Engineering"));

        var result = adapter.save(book);

        assertThat(result.id()).isEqualTo(1L);
        assertThat(result.title()).isEqualTo("Clean Code");
        assertThat(result.author()).isEqualTo("Robert C. Martin");
        assertThat(result.isbn()).isEqualTo("978-0132350884");
        assertThat(result.genre()).isEqualTo("Software Engineering");
    }

    @Test
    void findAllMapsEntitiesToDomainBooks() {
        when(jpaBookRepository.findAll()).thenReturn(List.of(
                new BookEntity(1L, "Clean Code", "Robert C. Martin", "978-0132350884", "Software Engineering"),
                new BookEntity(2L, "The Pragmatic Programmer", "David Thomas", "978-0135957059", "Software Engineering")
        ));

        var result = adapter.findAll();

        assertThat(result).hasSize(2);
        assertThat(result).extracting(Book::title)
                .containsExactly("Clean Code", "The Pragmatic Programmer");
    }

    @Test
    void findByIdReturnsMappedBookWhenFound() {
        when(jpaBookRepository.findById(1L))
                .thenReturn(Optional.of(new BookEntity(1L, "Clean Code", "Robert C. Martin", "978-0132350884", "Software Engineering")));

        var result = adapter.findById(1L);

        assertThat(result).isPresent();
        assertThat(result.get().id()).isEqualTo(1L);
        assertThat(result.get().title()).isEqualTo("Clean Code");
    }

    @Test
    void findByIdReturnsEmptyWhenNotFound() {
        when(jpaBookRepository.findById(99L)).thenReturn(Optional.empty());

        assertThat(adapter.findById(99L)).isEmpty();
    }

    @Test
    void savePassesCorrectEntityFields() {
        var book = new Book(null, "Clean Code", "Robert C. Martin", "978-0132350884", "Software Engineering");
        when(jpaBookRepository.save(any(BookEntity.class)))
                .thenAnswer(inv -> {
                    BookEntity e = inv.getArgument(0);
                    return new BookEntity(1L, e.getTitle(), e.getAuthor(), e.getIsbn(), e.getGenre());
                });

        adapter.save(book);

        verify(jpaBookRepository).save(any(BookEntity.class));
    }
}
