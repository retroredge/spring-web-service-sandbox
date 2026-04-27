package uk.co.redsoft.sandbox.domain.ports.in;

import uk.co.redsoft.sandbox.domain.model.Book;

import java.util.List;
import java.util.Optional;

public interface BookQueryUseCase {
    List<Book> findAll();
    Optional<Book> findById(Long id);
}
