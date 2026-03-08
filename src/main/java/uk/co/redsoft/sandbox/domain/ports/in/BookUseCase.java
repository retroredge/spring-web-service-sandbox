package uk.co.redsoft.sandbox.domain.ports.in;

import uk.co.redsoft.sandbox.domain.model.Book;
import uk.co.redsoft.sandbox.domain.model.CreateBookCommand;

import java.util.List;
import java.util.Optional;

public interface BookUseCase {
    List<Book> findAll();
    Optional<Book> findById(Long id);
    Book create(CreateBookCommand command);
    void importBook(CreateBookCommand command);
}
