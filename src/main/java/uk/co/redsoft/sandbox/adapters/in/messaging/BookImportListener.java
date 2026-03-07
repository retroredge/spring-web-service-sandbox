package uk.co.redsoft.sandbox.adapters.in.messaging;

import io.micrometer.core.annotation.Timed;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import uk.co.redsoft.sandbox.config.RabbitConfig;
import uk.co.redsoft.sandbox.domain.model.Book;
import uk.co.redsoft.sandbox.domain.ports.in.BookUseCase;

@Slf4j
@RequiredArgsConstructor
@Component
public class BookImportListener {

    private final BookUseCase bookUseCase;

    @Timed("books.import.listener")
    @RabbitListener(queues = RabbitConfig.QUEUE)
    public void onMessage(BookImportMessage message) {
        log.debug("Received message for book: '{}'", message.title());
        bookUseCase.create(new Book(null, message.title(), message.author(), message.isbn(), message.genre()));
    }
}
