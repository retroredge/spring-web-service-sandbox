package uk.co.redsoft.sandbox.domain.usecase;

import uk.co.redsoft.sandbox.domain.model.Book;
import uk.co.redsoft.sandbox.domain.model.BookAlreadyExistsException;
import uk.co.redsoft.sandbox.domain.model.BookNotFoundException;
import uk.co.redsoft.sandbox.domain.model.CreateBookCommand;
import uk.co.redsoft.sandbox.domain.ports.in.BookUseCase;
import uk.co.redsoft.sandbox.domain.ports.out.BookImportPort;
import uk.co.redsoft.sandbox.domain.ports.out.BookStore;

import java.util.List;
import java.util.Optional;

public class BookService implements BookUseCase {

    private final BookStore bookStore;
    private final BookImportPort bookImportPort;

    public BookService(BookStore bookStore, BookImportPort bookImportPort) {
        this.bookStore = bookStore;
        this.bookImportPort = bookImportPort;
    }

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
        if (bookStore.existsByIsbn(command.isbn())) {
            throw new BookAlreadyExistsException(command.isbn());
        }
        var book = new Book(null, command.title(), command.author(), command.isbn(), command.genre());
        return bookStore.save(book);
    }

    @Override
    public void importBook(CreateBookCommand command) {
        bookImportPort.submitForImport(command);
    }

    @Override
    public Book update(Long id, CreateBookCommand command) {
        if (!bookStore.findById(id).isPresent()) {
            throw new BookNotFoundException(id);
        }
        var book = new Book(id, command.title(), command.author(), command.isbn(), command.genre());
        return bookStore.save(book);
    }

    @Override
    public void delete(Long id) {
        if (!bookStore.findById(id).isPresent()) {
            throw new BookNotFoundException(id);
        }
        bookStore.deleteById(id);
    }
}
