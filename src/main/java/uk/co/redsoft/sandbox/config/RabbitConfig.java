package uk.co.redsoft.sandbox.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitConfig {

    public static final String EXCHANGE = "books.exchange";
    public static final String QUEUE = "books.import";
    public static final String ROUTING_KEY = "books.import";
    public static final String DLQ = "books.import.dlq";
    public static final String DLX = "books.dlx";

    @Bean
    DirectExchange booksExchange() {
        return new DirectExchange(EXCHANGE);
    }

    @Bean
    DirectExchange deadLetterExchange() {
        return new DirectExchange(DLX);
    }

    @Bean
    Queue queue() {
        return QueueBuilder.durable(QUEUE)
                .deadLetterExchange(DLX)
                .deadLetterRoutingKey(DLQ)
                .build();
    }

    @Bean
    Queue deadLetterQueue() {
        return QueueBuilder.durable(DLQ).build();
    }

    @Bean
    Binding binding(Queue queue, DirectExchange booksExchange) {
        return BindingBuilder.bind(queue).to(booksExchange).with(ROUTING_KEY);
    }

    @Bean
    Binding deadLetterBinding(Queue deadLetterQueue, DirectExchange deadLetterExchange) {
        return BindingBuilder.bind(deadLetterQueue).to(deadLetterExchange).with(DLQ);
    }

    @Bean
    MessageConverter messageConverter() {
        return new JacksonJsonMessageConverter("uk.co.redsoft.sandbox.domain.model");
    }
}
