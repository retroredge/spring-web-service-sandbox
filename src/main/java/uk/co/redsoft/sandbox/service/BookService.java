package uk.co.redsoft.sandbox.service;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.csv.CSVFormat;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import uk.co.redsoft.sandbox.config.RabbitConfig;
import uk.co.redsoft.sandbox.model.Book;
import uk.co.redsoft.sandbox.repository.BookEntity;
import uk.co.redsoft.sandbox.repository.BookRepository;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
public class BookService {

    private final BookRepository bookRepository;
    private final AmqpTemplate amqpTemplate;

    public BookService(BookRepository bookRepository, AmqpTemplate amqpTemplate) {
        this.bookRepository = bookRepository;
        this.amqpTemplate = amqpTemplate;
    }

    public List<Book> findAll() {
        return bookRepository.findAll().stream()
                .map(this::toBook)
                .toList();
    }

    public Optional<Book> findById(Long id) {
        return bookRepository.findById(id).map(this::toBook);
    }

    public Book create(Book newBook) {
        var entity = new BookEntity(newBook.title(), newBook.author(), newBook.isbn(), newBook.genre());
        return toBook(bookRepository.save(entity));
    }

    public void importCsv(MultipartFile file) throws IOException {
        try (var reader = new InputStreamReader(file.getInputStream());
             var parser = CSVFormat.DEFAULT.builder()
                     .setHeader()
                     .setSkipHeaderRecord(true)
                     .build()
                     .parse(reader)) {
            var records = parser.getRecords();
            log.debug("Queuing {} book(s) from CSV import", records.size());
            for (var record : records) {
                var entity = new BookEntity(record.get("title"), record.get("author"), record.get("isbn"), record.get("genre"));
                amqpTemplate.convertAndSend(RabbitConfig.QUEUE, entity);
            }
        }
    }

    private Book toBook(BookEntity entity) {
        return new Book(entity.getId(), entity.getTitle(), entity.getAuthor(), entity.getIsbn(), entity.getGenre());
    }
}
