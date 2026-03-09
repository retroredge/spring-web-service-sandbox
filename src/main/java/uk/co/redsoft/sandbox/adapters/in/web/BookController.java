package uk.co.redsoft.sandbox.adapters.in.web;

import jakarta.validation.Valid;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.csv.CSVFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import uk.co.redsoft.sandbox.domain.model.Book;
import uk.co.redsoft.sandbox.domain.model.CreateBookCommand;
import uk.co.redsoft.sandbox.domain.ports.in.BookUseCase;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.util.List;
import java.util.Map;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/books")
public class BookController {

    private final BookUseCase bookUseCase;
    private final Validator validator;

    @GetMapping
    public List<Book> getAllBooks() {
        return bookUseCase.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Book> getBook(@PathVariable Long id) {
        return bookUseCase.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Book> createBook(@Valid @RequestBody CreateBookRequest request) {
        var book = bookUseCase.create(new CreateBookCommand(request.title(), request.author(), request.isbn(), request.genre()));
        return ResponseEntity.created(URI.create("/books/" + book.id())).body(book);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Book> updateBook(@PathVariable Long id, @Valid @RequestBody CreateBookRequest request) {
        var book = bookUseCase.update(id, new CreateBookCommand(request.title(), request.author(), request.isbn(), request.genre()));
        return ResponseEntity.ok(book);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteBook(@PathVariable Long id) {
        bookUseCase.delete(id);
    }

    @PostMapping(value = "/import", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.ACCEPTED)
    public Map<String, Integer> importBooks(@RequestParam MultipartFile file) throws IOException {
        int enqueued = 0;
        int skipped = 0;
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
                    skipped++;
                    continue;
                }
                bookUseCase.importBook(new CreateBookCommand(request.title(), request.author(), request.isbn(), request.genre()));
                enqueued++;
            }
        }
        return Map.of("enqueued", enqueued, "skipped", skipped);
    }
}
