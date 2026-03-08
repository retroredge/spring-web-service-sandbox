package uk.co.redsoft.sandbox.adapters.out.persistence;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import uk.co.redsoft.sandbox.domain.model.Book;
import uk.co.redsoft.sandbox.domain.model.CreateBookCommand;
import uk.co.redsoft.sandbox.domain.ports.out.BookStore;

import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@Component
public class BookRepositoryAdapter implements BookStore {

    private final JpaBookRepository jpaBookRepository;

    @Override
    public Book save(CreateBookCommand command) {
        return toBook(jpaBookRepository.save(toEntity(command)));
    }

    @Override
    public List<Book> findAll() {
        return jpaBookRepository.findAll().stream().map(this::toBook).toList();
    }

    @Override
    public Optional<Book> findById(Long id) {
        return jpaBookRepository.findById(id).map(this::toBook);
    }

    private Book toBook(BookEntity entity) {
        return new Book(entity.getId(), entity.getTitle(), entity.getAuthor(), entity.getIsbn(), entity.getGenre());
    }

    private BookEntity toEntity(CreateBookCommand command) {
        return new BookEntity(command.title(), command.author(), command.isbn(), command.genre());
    }
}
