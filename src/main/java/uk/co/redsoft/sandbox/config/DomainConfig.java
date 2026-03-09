package uk.co.redsoft.sandbox.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import uk.co.redsoft.sandbox.domain.ports.in.BookUseCase;
import uk.co.redsoft.sandbox.domain.ports.out.BookImportPort;
import uk.co.redsoft.sandbox.domain.ports.out.BookStore;
import uk.co.redsoft.sandbox.domain.usecase.BookService;

@Configuration
public class DomainConfig {

    @Bean
    BookUseCase bookUseCase(BookStore bookStore, BookImportPort bookImportPort) {
        return new BookService(bookStore, bookImportPort);
    }
}
