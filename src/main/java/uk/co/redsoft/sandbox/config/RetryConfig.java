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

    @Bean
    StatefulRetryOperationsInterceptor pricingRetryInterceptor(RabbitTemplate rabbitTemplate) {
        return RetryInterceptorBuilder.stateful()
                .maxAttempts(4)
                // initialInterval=2s, multiplier=2.0, maxInterval=8s → waits of 2s, 4s, 8s
                .backOffOptions(2000, 2.0, 8000)
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
