package uk.co.redsoft.sandbox.config;

import org.springframework.amqp.core.Queue;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitConfig {

    public static final String QUEUE = "books.import";
    public static final String PRICING_QUEUE = "books.pricing";

    @Bean
    Queue queue() {
        return new Queue(QUEUE);
    }

    @Bean
    Queue pricingQueue() {
        return new Queue(PRICING_QUEUE);
    }

    @Bean
    MessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter("uk.co.redsoft.sandbox.domain.model");
    }
}
