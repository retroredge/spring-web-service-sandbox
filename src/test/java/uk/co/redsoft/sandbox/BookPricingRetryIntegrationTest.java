package uk.co.redsoft.sandbox;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import uk.co.redsoft.sandbox.adapters.out.messaging.RabbitBookPricingPublishAdapter;
import uk.co.redsoft.sandbox.config.RabbitConfig;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

@SpringBootTest
class BookPricingRetryIntegrationTest extends AbstractWireMockContainersIntegrationTest {

    private static final long RECEIVE_TIMEOUT_MS = 1_000;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private RabbitBookPricingPublishAdapter bookPricingPublishAdapter;

    @Test
    void whenPriceCatalogueReturns5xx_messageIsDeadLettered() {
        bookPricingPublishAdapter.publish("978-0000000000");

        var dlqMessage = await().atMost(30, TimeUnit.SECONDS)
                .until(() -> rabbitTemplate.receive(RabbitConfig.PRICING_DLQ, RECEIVE_TIMEOUT_MS), Objects::nonNull);

        assertThat(dlqMessage.getMessageProperties().getHeaders())
                .containsKey("x-exception-message");

        var residual = rabbitTemplate.receive(RabbitConfig.PRICING_QUEUE, RECEIVE_TIMEOUT_MS);
        assertThat(residual).isNull();
    }
}
