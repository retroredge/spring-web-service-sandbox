package uk.co.redsoft.sandbox;

import org.junit.jupiter.api.Test;
import org.springframework.amqp.core.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import uk.co.redsoft.sandbox.adapters.out.messaging.RabbitBookPricingPublishAdapter;
import uk.co.redsoft.sandbox.config.RabbitConfig;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

@SpringBootTest
class BookPricingRetryIntegrationTest extends AbstractWireMockContainersIntegrationTest {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private RabbitBookPricingPublishAdapter bookPricingPublishAdapter;

    @Test
    void whenPriceCatalogueReturns5xx_messageIsDeadLettered() {
        bookPricingPublishAdapter.publish("978-0000000000");

        var dlqMessage = new AtomicReference<Message>();
        await().atMost(30, TimeUnit.SECONDS).untilAsserted(() -> {
            var msg = rabbitTemplate.receive(RabbitConfig.PRICING_DLQ, 1000);
            assertThat(msg).isNotNull();
            dlqMessage.set(msg);
        });

        assertThat(dlqMessage.get().getMessageProperties().getHeaders())
                .containsKey("x-exception-message");

        var residual = rabbitTemplate.receive(RabbitConfig.PRICING_QUEUE, 1000);
        assertThat(residual).isNull();
    }
}
