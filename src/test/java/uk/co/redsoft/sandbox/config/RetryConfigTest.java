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
    void pricingRetryInterceptorMaxAttempts() {
        assertThat(RetryConfig.MAX_ATTEMPTS).isEqualTo(4);
    }

    @Test
    void pricingRetryInterceptorBackoffParams() {
        assertThat(RetryConfig.INITIAL_BACKOFF_MS).isEqualTo(2_000L);
        assertThat(RetryConfig.BACKOFF_MULTIPLIER).isEqualTo(2.0);
        assertThat(RetryConfig.MAX_BACKOFF_MS).isEqualTo(8_000L);
    }

    @Test
    void retryingContainerFactoryIsConfigured() {
        var interceptor = config.pricingRetryInterceptor(rabbitTemplate);
        var factory = config.retryingContainerFactory(connectionFactory, messageConverter, interceptor);
        assertThat(factory).isNotNull();
    }
}
