package uk.co.redsoft.sandbox.domain.usecase;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.csv.CSVFormat;
import org.springframework.stereotype.Service;
import uk.co.redsoft.sandbox.domain.model.Book;
import uk.co.redsoft.sandbox.domain.ports.in.BookUseCase;
import uk.co.redsoft.sandbox.domain.ports.out.BookImportPort;
import uk.co.redsoft.sandbox.domain.ports.out.BookStore;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Optional;

@Slf4j
@RequiredArgsConstructor
@Service
public class BookService implements BookUseCase {

    private final BookStore bookStore;
    private final BookImportPort bookImportPort;

    @Override
    public List<Book> findAll() {
        return bookStore.findAll();
    }

    @Override
    public Optional<Book> findById(Long id) {
        return bookStore.findById(id);
    }

    @Override
    public Book create(Book book) {
        return bookStore.save(book);
    }

    @Override
    public void importBooks(InputStream csv) throws IOException {
        try (var reader = new InputStreamReader(csv);
             var parser = CSVFormat.DEFAULT.builder()
                     .setHeader()
                     .setSkipHeaderRecord(true)
                     .get()
                     .parse(reader)) {
            for (var record : parser) {
                bookImportPort.enqueue(new Book(null, record.get("title"), record.get("author"), record.get("isbn"), record.get("genre")));
            }
        }
    }
}
