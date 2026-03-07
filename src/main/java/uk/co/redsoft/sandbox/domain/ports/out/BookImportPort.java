package uk.co.redsoft.sandbox.domain.ports.out;

import uk.co.redsoft.sandbox.domain.model.Book;

public interface BookImportPort {
    void enqueue(Book book);
}
