package uk.co.redsoft.sandbox.service;

import io.micrometer.core.annotation.Timed;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;
import uk.co.redsoft.sandbox.config.RabbitConfig;
import uk.co.redsoft.sandbox.repository.BookEntity;
import uk.co.redsoft.sandbox.repository.BookRepository;

@Slf4j
@Service
public class BookImportListener {

    private final BookRepository bookRepository;

    public BookImportListener(BookRepository bookRepository) {
        this.bookRepository = bookRepository;
    }

    @Timed("books.import.listener")
    @RabbitListener(queues = RabbitConfig.QUEUE)
    public void onMessage(BookEntity entity) {
        log.debug("Received message for book: '{}'", entity.getTitle());
        bookRepository.save(entity);
    }
}
