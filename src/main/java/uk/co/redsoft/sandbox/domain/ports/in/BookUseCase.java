package uk.co.redsoft.sandbox.domain.ports.in;

import uk.co.redsoft.sandbox.domain.model.Book;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Optional;

public interface BookUseCase {
    List<Book> findAll();
    Optional<Book> findById(Long id);
    Book create(Book book);
    void importBooks(InputStream csv) throws IOException;
}
