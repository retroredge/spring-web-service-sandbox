package uk.co.redsoft.sandbox.adapters.in.web;

import jakarta.validation.Valid;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.csv.CSVFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import uk.co.redsoft.sandbox.domain.model.Book;
import uk.co.redsoft.sandbox.domain.model.BookDetail;
import uk.co.redsoft.sandbox.domain.model.CreateBookCommand;
import uk.co.redsoft.sandbox.domain.ports.in.BookDetailUseCase;
import uk.co.redsoft.sandbox.domain.ports.in.BookUseCase;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/books")
public class BookController {

    private final BookUseCase bookUseCase;
    private final BookDetailUseCase bookDetailUseCase;
    private final Validator validator;

    @GetMapping
    public List<Book> getAllBooks() {
        return bookUseCase.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<BookDetail> getBook(@PathVariable Long id) {
        return bookDetailUseCase.getBookDetail(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Book> createBook(@Valid @RequestBody CreateBookRequest request) {
        var book = bookUseCase.create(new CreateBookCommand(request.title(), request.author(), request.isbn(), request.genre()));
        return ResponseEntity.created(URI.create("/books/" + book.id())).body(book);
    }

    @PostMapping(value = "/import", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.ACCEPTED)
    public void importBooks(@RequestParam MultipartFile file) throws IOException {
        try (var reader = new InputStreamReader(file.getInputStream());
             var parser = CSVFormat.DEFAULT.builder()
                     .setHeader()
                     .setSkipHeaderRecord(true)
                     .get()
                     .parse(reader)) {
            // The parser here is lazy and won't load the entire file into memory
            for (var record : parser) {
                var request = new CreateBookRequest(record.get("title"), record.get("author"), record.get("isbn"), record.get("genre"));
                var violations = validator.validate(request);
                if (!violations.isEmpty()) {
                    log.warn("Skipping invalid CSV row {}: {}", record.getRecordNumber(),
                            violations.stream().map(v -> v.getPropertyPath() + " " + v.getMessage()).toList());
                    continue;
                }
                bookUseCase.importBook(new CreateBookCommand(request.title(), request.author(), request.isbn(), request.genre()));
            }
        }
    }

}
