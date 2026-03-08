package uk.co.redsoft.sandbox.config;

import org.springframework.amqp.core.Queue;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitConfig {

    public static final String QUEUE = "books.import";

    @Bean
    Queue queue() {
        return new Queue(QUEUE);
    }

    @Bean
    MessageConverter messageConverter() {
        return new JacksonJsonMessageConverter("uk.co.redsoft.sandbox.domain.model");
    }
}
