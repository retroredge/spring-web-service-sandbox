package uk.co.redsoft.sandbox.config;

import org.springframework.amqp.rabbit.config.RetryInterceptorBuilder;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.retry.RepublishMessageRecoverer;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.retry.interceptor.StatefulRetryOperationsInterceptor;

@Configuration
public class RetryConfig {

    static final int MAX_ATTEMPTS = 4;
    static final long INITIAL_BACKOFF_MS = 2_000;
    static final double BACKOFF_MULTIPLIER = 2.0;
    static final long MAX_BACKOFF_MS = 8_000;

    @Bean
    StatefulRetryOperationsInterceptor pricingRetryInterceptor(RabbitTemplate rabbitTemplate) {
        return RetryInterceptorBuilder.stateful()
                .maxAttempts(MAX_ATTEMPTS)
                // initialInterval=2s, multiplier=2.0, maxInterval=8s → waits of 2s, 4s, 8s
                .backOffOptions(INITIAL_BACKOFF_MS, BACKOFF_MULTIPLIER, MAX_BACKOFF_MS)
                // routes via the default exchange — empty string means default exchange, queue name is the routing key
                .recoverer(new RepublishMessageRecoverer(rabbitTemplate, "", RabbitConfig.PRICING_DLQ))
                .build();
    }

    @Bean
    SimpleRabbitListenerContainerFactory retryingContainerFactory(
            ConnectionFactory connectionFactory,
            MessageConverter messageConverter,
            StatefulRetryOperationsInterceptor pricingRetryInterceptor) {
        var factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(messageConverter);
        factory.setAdviceChain(pricingRetryInterceptor);
        return factory;
    }
}
