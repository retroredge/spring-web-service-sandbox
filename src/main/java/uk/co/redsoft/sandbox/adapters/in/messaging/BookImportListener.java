package uk.co.redsoft.sandbox.adapters.in.messaging;

import io.micrometer.core.annotation.Timed;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import uk.co.redsoft.sandbox.config.RabbitConfig;
import uk.co.redsoft.sandbox.domain.model.CreateBookCommand;
import uk.co.redsoft.sandbox.domain.ports.in.BookWriteUseCase;

@Slf4j
@RequiredArgsConstructor
@Component
public class BookImportListener {

    private final BookWriteUseCase bookWriteUseCase;

    @Timed("books.import.listener")
    @RabbitListener(queues = RabbitConfig.QUEUE)
    public void onMessage(CreateBookCommand command) {
        log.debug("Received message for book: '{}'", command.title());
        bookWriteUseCase.create(command);
    }
}
