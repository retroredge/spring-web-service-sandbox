package uk.co.redsoft.sandbox.adapters.out.messaging;

import lombok.RequiredArgsConstructor;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.stereotype.Component;
import uk.co.redsoft.sandbox.adapters.in.messaging.BookImportMessage;
import uk.co.redsoft.sandbox.config.RabbitConfig;
import uk.co.redsoft.sandbox.domain.model.Book;
import uk.co.redsoft.sandbox.domain.ports.out.BookImportPort;

@RequiredArgsConstructor
@Component
public class RabbitBookPublishAdapter implements BookImportPort {

    private final AmqpTemplate amqpTemplate;

    @Override
    public void enqueue(Book book) {
        amqpTemplate.convertAndSend(RabbitConfig.QUEUE,
                new BookImportMessage(book.title(), book.author(), book.isbn(), book.genre()));
    }
}
