package uk.co.redsoft.sandbox.adapters.out.messaging;

import lombok.RequiredArgsConstructor;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.stereotype.Component;
import uk.co.redsoft.sandbox.config.RabbitConfig;
import uk.co.redsoft.sandbox.domain.model.BookPricingMessage;
import uk.co.redsoft.sandbox.domain.ports.out.BookPricingPublishPort;

@RequiredArgsConstructor
@Component
public class RabbitBookPricingPublishAdapter implements BookPricingPublishPort {

    private final AmqpTemplate amqpTemplate;

    @Override
    public void publish(String isbn) {
        amqpTemplate.convertAndSend(RabbitConfig.PRICING_QUEUE, new BookPricingMessage(isbn));
    }
}
