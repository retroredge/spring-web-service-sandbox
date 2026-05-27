package uk.co.redsoft.sandbox.config;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.core.MessageBuilder;
import org.springframework.amqp.core.MessagePostProcessor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import java.util.ArrayList;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;

@ExtendWith(MockitoExtension.class)
class RabbitConfigTest {

    private final RabbitConfig config = new RabbitConfig();

    @Mock
    RabbitTemplate rabbitTemplate;

    @Test
    void pricingDlqHasCorrectQueueName() {
        assertThat(config.pricingDlq().getName()).isEqualTo("books.pricing.dlq");
    }

    @Test
    void messageIdCustomizerSetsUuidMessageId() {
        var processors = new ArrayList<MessagePostProcessor>();
        doAnswer(inv -> { processors.add(inv.getArgument(0)); return null; })
            .when(rabbitTemplate).addBeforePublishPostProcessors(any(MessagePostProcessor.class));

        config.messageIdCustomizer().customize(rabbitTemplate);

        assertThat(processors).hasSize(1);
        var message = MessageBuilder.withBody("{}".getBytes()).build();
        processors.get(0).postProcessMessage(message);
        assertThat(message.getMessageProperties().getMessageId()).isNotBlank();
    }
}
