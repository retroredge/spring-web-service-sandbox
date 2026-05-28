# Pricing Listener Retry and Dead-Letter Queue

**Date:** 2026-05-22

## Problem

When the price catalogue REST endpoint returns a 5xx error, `PriceCatalogueException` propagates uncaught through `PriceLookupService` and `BookPricingListener` to the Spring AMQP listener container. The container NACKs the message and requeues it immediately — creating a tight infinite loop that hammers the downstream service for as long as it remains unhealthy. There is no retry backoff, no retry limit, and no dead-letter queue.

## Solution

Spring AMQP stateful retry on the `books.pricing` listener with `RepublishMessageRecoverer` routing exhausted messages to `books.pricing.dlq`.

- Between attempts the message is NACKd and requeued to `books.pricing` (the same queue).
- Retry state is tracked in-memory keyed by message ID.
- After 3 retries (4 total attempts) the recoverer publishes the message to `books.pricing.dlq` with exception metadata in the headers.
- Two queues total: `books.pricing` (unchanged), `books.pricing.dlq` (new).

**Retry parameters:** initial interval 2s, multiplier 2.0, max interval 8s → waits of 2s, 4s, 8s between attempts.

**Trade-off:** retry state is in-memory, so an app restart mid-retry resets the counter and the message gets a fresh 3 attempts. Acceptable for this use case.

## Components

### 1. `RabbitConfig` — declare DLQ

Add a `books.pricing.dlq` queue constant and bean. No changes to the `books.pricing` queue declaration — requeueing is driven by NACK, not `x-dead-letter-exchange`.

```java
public static final String PRICING_DLQ = "books.pricing.dlq";

@Bean
Queue pricingDlq() {
    return new Queue(PRICING_DLQ);
}
```

Also add a `RabbitTemplateCustomizer` bean that stamps a UUID `messageId` onto every outbound message. Stateful retry uses `messageId` as its cache key; without it all messages would share the same retry state.

```java
@Bean
RabbitTemplateCustomizer messageIdCustomizer() {
    return template -> template.setBeforePublishPostProcessors(msg -> {
        msg.getMessageProperties().setMessageId(UUID.randomUUID().toString());
        return msg;
    });
}
```

### 2. `RetryConfig` — new configuration class

Wires the retry interceptor and a named container factory scoped to the pricing listener, leaving `books.import` unaffected.

```java
@Configuration
public class RetryConfig {

    @Bean
    RetryOperationsInterceptor pricingRetryInterceptor(RabbitTemplate rabbitTemplate) {
        return RetryInterceptorBuilder.stateful()
                .maxAttempts(4)                          // 1 initial + 3 retries
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
            RetryOperationsInterceptor pricingRetryInterceptor) {
        var factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(messageConverter);
        factory.setAdviceChain(pricingRetryInterceptor);
        return factory;
    }
}
```

### 3. `BookPricingListener` — use named container factory

```java
@RabbitListener(id = "bookPricingListener", queues = RabbitConfig.PRICING_QUEUE, containerFactory = "retryingContainerFactory")
```

## Testing

### `RetryConfigTest` (unit)

Verifies the `retryingContainerFactory` bean is present and has an advice chain configured. Follows the pattern of `PriceCatalogueConfigTest`.

### `BookPricingRetryIntegrationTest` (integration)

Extends `AbstractWireMockContainersIntegrationTest` (real RabbitMQ via Testcontainers, WireMock for HTTP). Stubs the price catalogue to return 5xx. Publishes an ISBN to `books.pricing`. Asserts:
- The message arrives on `books.pricing.dlq` after the retry sequence completes.
- `books.pricing` is empty.

### `BookPricingListenerTest` (existing)

Review for any impact from the `containerFactory` annotation change; update if needed.
