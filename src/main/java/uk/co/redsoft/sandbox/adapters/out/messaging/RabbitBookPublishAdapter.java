package uk.co.redsoft.sandbox.adapters.out.messaging;

import lombok.RequiredArgsConstructor;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.stereotype.Component;
import uk.co.redsoft.sandbox.config.RabbitConfig;
import uk.co.redsoft.sandbox.domain.model.CreateBookCommand;
import uk.co.redsoft.sandbox.domain.ports.out.BookImportPort;

@RequiredArgsConstructor
@Component
public class RabbitBookPublishAdapter implements BookImportPort {

    private final AmqpTemplate amqpTemplate;

    @Override
    public void enqueue(CreateBookCommand command) {
        amqpTemplate.convertAndSend(RabbitConfig.QUEUE, command);
    }
}
