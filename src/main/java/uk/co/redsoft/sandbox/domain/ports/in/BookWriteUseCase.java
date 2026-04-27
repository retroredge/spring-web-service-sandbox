package uk.co.redsoft.sandbox.domain.ports.in;

import uk.co.redsoft.sandbox.domain.model.Book;
import uk.co.redsoft.sandbox.domain.model.CreateBookCommand;

public interface BookWriteUseCase {
    Book create(CreateBookCommand command);
    void importBook(CreateBookCommand command);
}
