package uk.co.redsoft.sandbox.config;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.retry.interceptor.StatefulRetryOperationsInterceptor;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class RetryConfigTest {

    @Mock private RabbitTemplate rabbitTemplate;
    @Mock private ConnectionFactory connectionFactory;
    @Mock private MessageConverter messageConverter;

    private final RetryConfig config = new RetryConfig();

    @Test
    void pricingRetryInterceptorIsStateful() {
        var interceptor = config.pricingRetryInterceptor(rabbitTemplate);

        assertThat(interceptor).isInstanceOf(StatefulRetryOperationsInterceptor.class);
    }

    @Test
    void retryingContainerFactoryIsConfigured() {
        var interceptor = config.pricingRetryInterceptor(rabbitTemplate);
        var factory = config.retryingContainerFactory(connectionFactory, messageConverter, interceptor);

        assertThat(factory).isNotNull();
    }
}
