package uk.co.redsoft.sandbox.domain.ports.out;

import uk.co.redsoft.sandbox.domain.model.Book;
import uk.co.redsoft.sandbox.domain.model.CreateBookCommand;

import java.util.List;
import java.util.Optional;

public interface BookStore {
    Book save(CreateBookCommand command);
    List<Book> findAll();
    Optional<Book> findById(Long id);
}
