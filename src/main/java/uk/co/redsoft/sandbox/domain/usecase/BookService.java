package uk.co.redsoft.sandbox.domain.usecase;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.co.redsoft.sandbox.domain.model.Book;
import uk.co.redsoft.sandbox.domain.model.CreateBookCommand;
import uk.co.redsoft.sandbox.domain.ports.in.BookWriteUseCase;
import uk.co.redsoft.sandbox.domain.ports.in.BookQueryUseCase;
import uk.co.redsoft.sandbox.domain.ports.out.BookImportPort;
import uk.co.redsoft.sandbox.domain.ports.out.BookPricingPublishPort;
import uk.co.redsoft.sandbox.domain.ports.out.BookRepositoryPort;

import java.util.List;
import java.util.Optional;

@Slf4j
@RequiredArgsConstructor
@Service
public class BookService implements BookQueryUseCase, BookWriteUseCase {

    private final BookRepositoryPort bookStore;
    private final BookImportPort bookImportPort;
    private final BookPricingPublishPort bookPricingPublishPort;

    @Override
    public List<Book> findAll() {
        return bookStore.findAll();
    }

    @Override
    public Optional<Book> findById(Long id) {
        return bookStore.findById(id);
    }

    @Override
    public Book create(CreateBookCommand command) {
        var book = bookStore.save(new Book(null, command.title(), command.author(), command.isbn(), command.genre()));
        bookPricingPublishPort.publish(book.isbn());
        return book;
    }

    @Override
    public void importBook(CreateBookCommand command) {
        bookImportPort.enqueue(command);
    }
}
