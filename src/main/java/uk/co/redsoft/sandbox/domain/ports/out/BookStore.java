package uk.co.redsoft.sandbox.domain.ports.out;

import uk.co.redsoft.sandbox.domain.model.Book;

import java.util.List;
import java.util.Optional;

public interface BookStore {
    Book save(Book book);
    List<Book> findAll();
    Optional<Book> findById(Long id);
    boolean existsByIsbn(String isbn);
}
