package uk.co.redsoft.sandbox.adapters.in.messaging;

import io.micrometer.core.annotation.Timed;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import uk.co.redsoft.sandbox.config.RabbitConfig;
import uk.co.redsoft.sandbox.domain.model.BookPricingMessage;
import uk.co.redsoft.sandbox.domain.ports.in.PriceLookupUseCase;

@Slf4j
@RequiredArgsConstructor
@Component
public class BookPricingListener {

    private final PriceLookupUseCase priceLookupUseCase;

    @Timed("books.pricing.listener")
    @RabbitListener(id = "bookPricingListener", queues = RabbitConfig.PRICING_QUEUE) // id must match ListenerContainerService.LISTENER_ID
    public void onMessage(BookPricingMessage message) {
        log.debug("Received pricing request for ISBN: {}", message.isbn());
        priceLookupUseCase.lookupAndStorePrices(message.isbn());
    }
}
