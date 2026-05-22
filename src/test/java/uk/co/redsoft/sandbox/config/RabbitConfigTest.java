package uk.co.redsoft.sandbox.config;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class RabbitConfigTest {

    private final RabbitConfig config = new RabbitConfig();

    @Test
    void pricingDlqHasCorrectQueueName() {
        assertThat(config.pricingDlq().getName()).isEqualTo("books.pricing.dlq");
    }
}
