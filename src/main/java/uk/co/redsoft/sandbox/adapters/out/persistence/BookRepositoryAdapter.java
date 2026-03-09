package uk.co.redsoft.sandbox.adapters.out.persistence;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import uk.co.redsoft.sandbox.domain.model.Book;
import uk.co.redsoft.sandbox.domain.ports.out.BookStore;

import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@Component
public class BookRepositoryAdapter implements BookStore {

    private final JpaBookRepository jpaBookRepository;

    @Override
    public Book save(Book book) {
        return toBook(jpaBookRepository.save(toEntity(book)));
    }

    @Override
    public List<Book> findAll() {
        return jpaBookRepository.findAll().stream().map(this::toBook).toList();
    }

    @Override
    public Optional<Book> findById(Long id) {
        return jpaBookRepository.findById(id).map(this::toBook);
    }

    @Override
    public boolean existsByIsbn(String isbn) {
        return jpaBookRepository.existsByIsbn(isbn);
    }

    @Override
    public void deleteById(Long id) {
        jpaBookRepository.deleteById(id);
    }

    private Book toBook(BookEntity entity) {
        return new Book(entity.getId(), entity.getTitle(), entity.getAuthor(), entity.getIsbn(), entity.getGenre());
    }

    private BookEntity toEntity(Book book) {
        if (book.id() != null) {
            return new BookEntity(book.id(), book.title(), book.author(), book.isbn(), book.genre());
        }
        return new BookEntity(book.title(), book.author(), book.isbn(), book.genre());
    }
}
