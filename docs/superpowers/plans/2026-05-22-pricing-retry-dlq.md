# Pricing Listener Retry and Dead-Letter Queue Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Add stateful retry with exponential backoff (2s/4s/8s, 3 retries) to the `books.pricing` listener so that 5xx errors from the price catalogue cause the ISBN message to be requeued and retried, then dead-lettered to `books.pricing.dlq` after exhaustion.

**Architecture:** Spring AMQP stateful retry intercepts exceptions thrown by `BookPricingListener`, NACKs the message, and requeues it to `books.pricing` between attempts. A retry state cache keyed by message ID tracks attempt counts in memory. After 3 retries `RepublishMessageRecoverer` publishes the message to `books.pricing.dlq`. A named container factory (`retryingContainerFactory`) scopes the retry behaviour to the pricing listener only.

**Tech Stack:** Spring AMQP (`RetryInterceptorBuilder`, `StatefulRetryOperationsInterceptor`, `RepublishMessageRecoverer`, `SimpleRabbitListenerContainerFactory`), `spring-retry`, Awaitility (already on classpath via `spring-boot-starter-test`), Testcontainers RabbitMQ, WireMock.

---

## File Map

| Action | File |
|--------|------|
| Modify | `src/main/java/uk/co/redsoft/sandbox/config/RabbitConfig.java` |
| Create | `src/main/java/uk/co/redsoft/sandbox/config/RetryConfig.java` |
| Modify | `src/main/java/uk/co/redsoft/sandbox/adapters/in/messaging/BookPricingListener.java` |
| Create | `src/test/java/uk/co/redsoft/sandbox/config/RabbitConfigTest.java` |
| Create | `src/test/java/uk/co/redsoft/sandbox/config/RetryConfigTest.java` |
| Create | `src/test/java/uk/co/redsoft/sandbox/BookPricingRetryIntegrationTest.java` |
| Create | `src/test/resources/wiremock/mappings/978-0000000000-prices.json` |
| Modify | `pom.xml` (add `spring-retry`) |

---

## Task 1: Declare `books.pricing.dlq` in `RabbitConfig`

**Files:**
- Create: `src/test/java/uk/co/redsoft/sandbox/config/RabbitConfigTest.java`
- Modify: `src/main/java/uk/co/redsoft/sandbox/config/RabbitConfig.java`

- [ ] **Step 1: Write the failing test**

Create `src/test/java/uk/co/redsoft/sandbox/config/RabbitConfigTest.java`:

```java
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
```

- [ ] **Step 2: Run the test to verify it fails**

```bash
./mvnw test -pl . -Dtest=RabbitConfigTest -q
```

Expected: FAIL — `pricingDlq()` does not exist yet.

- [ ] **Step 3: Add the DLQ constant and queue bean to `RabbitConfig`**

Open `src/main/java/uk/co/redsoft/sandbox/config/RabbitConfig.java` and add the constant and bean (alongside the existing `QUEUE` and `PRICING_QUEUE` declarations):

```java
public static final String PRICING_DLQ = "books.pricing.dlq";

@Bean
Queue pricingDlq() {
    return new Queue(PRICING_DLQ);
}
```

No other changes to `RabbitConfig` in this task.

- [ ] **Step 4: Run the test to verify it passes**

```bash
./mvnw test -pl . -Dtest=RabbitConfigTest -q
```

Expected: PASS.

- [ ] **Step 5: Commit**

```bash
git add src/main/java/uk/co/redsoft/sandbox/config/RabbitConfig.java \
        src/test/java/uk/co/redsoft/sandbox/config/RabbitConfigTest.java
git commit -m "Declare books.pricing.dlq queue"
```

---

## Task 2: Add `messageId` customizer to `RabbitConfig`

Stateful retry uses the message's `messageId` property as a cache key to correlate redeliveries to their retry state. Without a `messageId`, all messages share the same key and retry counts are wrong. This task adds a `RabbitTemplateCustomizer` that stamps a UUID onto every outbound message.

**Files:**
- Modify: `src/test/java/uk/co/redsoft/sandbox/config/RabbitConfigTest.java`
- Modify: `src/main/java/uk/co/redsoft/sandbox/config/RabbitConfig.java`

- [ ] **Step 1: Add the failing test to `RabbitConfigTest`**

Add this test to the existing `RabbitConfigTest` class. New imports needed: `org.mockito.junit.jupiter.MockitoExtension`, `org.junit.jupiter.api.extension.ExtendWith`, `org.mockito.Mock`, `org.mockito.Mockito.doAnswer`, `org.mockito.Mockito.any`, `org.springframework.amqp.core.MessageBuilder`, `org.springframework.amqp.core.MessagePostProcessor`, `org.springframework.amqp.rabbit.core.RabbitTemplate`.

Add `@ExtendWith(MockitoExtension.class)` to the class declaration, then add:

```java
@Mock
private RabbitTemplate rabbitTemplate;

@Test
void messageIdCustomizerSetsUuidMessageId() {
    var processors = new java.util.ArrayList<MessagePostProcessor>();
    doAnswer(inv -> { processors.add(inv.getArgument(0)); return null; })
        .when(rabbitTemplate).setBeforePublishPostProcessors(any(MessagePostProcessor.class));

    config.messageIdCustomizer().customize(rabbitTemplate);

    assertThat(processors).hasSize(1);
    var message = MessageBuilder.withBody("{}".getBytes()).build();
    processors.get(0).postProcessMessage(message);
    assertThat(message.getMessageProperties().getMessageId()).isNotBlank();
}
```

- [ ] **Step 2: Run the test to verify it fails**

```bash
./mvnw test -pl . -Dtest=RabbitConfigTest -q
```

Expected: FAIL — `messageIdCustomizer()` does not exist yet.

- [ ] **Step 3: Add the `messageIdCustomizer` bean to `RabbitConfig`**

Add the following imports to `RabbitConfig.java`:
```java
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.boot.autoconfigure.amqp.RabbitTemplateCustomizer;
import java.util.UUID;
```

Add the bean method to `RabbitConfig`:

```java
@Bean
RabbitTemplateCustomizer messageIdCustomizer() {
    return template -> template.setBeforePublishPostProcessors(msg -> {
        msg.getMessageProperties().setMessageId(UUID.randomUUID().toString());
        return msg;
    });
}
```

- [ ] **Step 4: Run the tests to verify they pass**

```bash
./mvnw test -pl . -Dtest=RabbitConfigTest -q
```

Expected: both tests PASS.

- [ ] **Step 5: Commit**

```bash
git add src/main/java/uk/co/redsoft/sandbox/config/RabbitConfig.java \
        src/test/java/uk/co/redsoft/sandbox/config/RabbitConfigTest.java
git commit -m "Stamp UUID messageId on outbound messages for stateful retry tracking"
```

---

## Task 3: Add `spring-retry` and create `RetryConfig`

`spring-retry` is an optional dependency of `spring-rabbit` and must be declared explicitly. `RetryConfig` wires the stateful retry interceptor and a named listener container factory.

**Files:**
- Modify: `pom.xml`
- Create: `src/test/java/uk/co/redsoft/sandbox/config/RetryConfigTest.java`
- Create: `src/main/java/uk/co/redsoft/sandbox/config/RetryConfig.java`

- [ ] **Step 1: Write the failing tests**

Create `src/test/java/uk/co/redsoft/sandbox/config/RetryConfigTest.java`:

```java
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
```

- [ ] **Step 2: Run the tests to verify they fail**

```bash
./mvnw test -pl . -Dtest=RetryConfigTest -q
```

Expected: FAIL — `RetryConfig` does not exist yet.

- [ ] **Step 3: Add `spring-retry` to `pom.xml`**

In `pom.xml`, inside `<dependencies>`, add (no version — managed by the Spring Boot BOM):

```xml
<dependency>
    <groupId>org.springframework.retry</groupId>
    <artifactId>spring-retry</artifactId>
</dependency>
```

- [ ] **Step 4: Create `RetryConfig`**

Create `src/main/java/uk/co/redsoft/sandbox/config/RetryConfig.java`:

```java
package uk.co.redsoft.sandbox.config;

import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.retry.RepublishMessageRecoverer;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.retry.interceptor.RetryInterceptorBuilder;
import org.springframework.retry.interceptor.RetryOperationsInterceptor;

@Configuration
public class RetryConfig {

    @Bean
    RetryOperationsInterceptor pricingRetryInterceptor(RabbitTemplate rabbitTemplate) {
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
            RetryOperationsInterceptor pricingRetryInterceptor) {
        var factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(messageConverter);
        factory.setAdviceChain(pricingRetryInterceptor);
        return factory;
    }
}
```

- [ ] **Step 5: Run the tests to verify they pass**

```bash
./mvnw test -pl . -Dtest=RetryConfigTest -q
```

Expected: both tests PASS.

- [ ] **Step 6: Commit**

```bash
git add pom.xml \
        src/main/java/uk/co/redsoft/sandbox/config/RetryConfig.java \
        src/test/java/uk/co/redsoft/sandbox/config/RetryConfigTest.java
git commit -m "Add RetryConfig with stateful retry interceptor and retryingContainerFactory"
```

---

## Task 4: Wire `retryingContainerFactory` into `BookPricingListener`

**Files:**
- Modify: `src/main/java/uk/co/redsoft/sandbox/adapters/in/messaging/BookPricingListener.java`

- [ ] **Step 1: Update the `@RabbitListener` annotation**

In `BookPricingListener.java`, change the `@RabbitListener` annotation on `onMessage` from:

```java
@RabbitListener(id = "bookPricingListener", queues = RabbitConfig.PRICING_QUEUE)
```

to:

```java
@RabbitListener(id = "bookPricingListener", queues = RabbitConfig.PRICING_QUEUE, containerFactory = "retryingContainerFactory")
```

- [ ] **Step 2: Run the existing unit test to verify no regression**

```bash
./mvnw test -pl . -Dtest=BookPricingListenerTest -q
```

Expected: PASS — the unit test uses `@InjectMocks` and does not involve Spring container or `@RabbitListener` processing, so it is unaffected.

- [ ] **Step 3: Run the full build to catch any wiring issues**

```bash
./mvnw verify -q
```

Expected: BUILD SUCCESS.

- [ ] **Step 4: Commit**

```bash
git add src/main/java/uk/co/redsoft/sandbox/adapters/in/messaging/BookPricingListener.java
git commit -m "Point BookPricingListener at retryingContainerFactory"
```

---

## Task 5: Integration test — retry exhaustion routes to DLQ

This test spins up real RabbitMQ (Testcontainers) and WireMock, publishes an ISBN whose price catalogue endpoint returns 500, and verifies the message ends up on `books.pricing.dlq` after the retry sequence completes (~14s minimum due to 2s+4s+8s backoffs).

**Files:**
- Create: `src/test/resources/wiremock/mappings/978-0000000000-prices.json`
- Create: `src/test/java/uk/co/redsoft/sandbox/BookPricingRetryIntegrationTest.java`

- [ ] **Step 1: Create the WireMock stub returning 500**

Create `src/test/resources/wiremock/mappings/978-0000000000-prices.json`:

```json
{
  "request": {
    "method": "GET",
    "url": "/catalogue/books/978-0000000000/prices",
    "basicAuthCredentials": {
      "username": "catalogue-user",
      "password": "secret"
    }
  },
  "response": {
    "status": 500,
    "body": "Internal Server Error"
  }
}
```

- [ ] **Step 2: Write the integration test**

Create `src/test/java/uk/co/redsoft/sandbox/BookPricingRetryIntegrationTest.java`:

```java
package uk.co.redsoft.sandbox;

import org.junit.jupiter.api.Test;
import org.springframework.amqp.core.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import uk.co.redsoft.sandbox.adapters.out.messaging.RabbitBookPricingPublishAdapter;
import uk.co.redsoft.sandbox.config.RabbitConfig;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

@SpringBootTest
class BookPricingRetryIntegrationTest extends AbstractWireMockContainersIntegrationTest {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private RabbitBookPricingPublishAdapter bookPricingPublishAdapter;

    @Test
    void whenPriceCatalogueReturns5xx_messageIsDeadLettered() {
        bookPricingPublishAdapter.publish("978-0000000000");

        var dlqMessage = new AtomicReference<Message>();
        await().atMost(30, TimeUnit.SECONDS).untilAsserted(() -> {
            var msg = rabbitTemplate.receive(RabbitConfig.PRICING_DLQ, 1000);
            assertThat(msg).isNotNull();
            dlqMessage.set(msg);
        });

        assertThat(dlqMessage.get().getMessageProperties().getHeaders())
                .containsKey("x-exception-message");

        var residual = rabbitTemplate.receive(RabbitConfig.PRICING_QUEUE, 1000);
        assertThat(residual).isNull();
    }
}
```

- [ ] **Step 3: Run the integration test to verify it passes**

This test takes ~20-25s due to retry backoffs.

```bash
./mvnw test -pl . -Dtest=BookPricingRetryIntegrationTest -q
```

Expected: PASS. If it times out, check that `RetryConfig` is being picked up (the `retryingContainerFactory` bean must exist and `BookPricingListener` must reference it) and that the WireMock stub file is being copied into the container correctly.

- [ ] **Step 4: Run the full test suite**

```bash
./mvnw verify -q
```

Expected: BUILD SUCCESS with all tests passing.

- [ ] **Step 5: Commit**

```bash
git add src/test/resources/wiremock/mappings/978-0000000000-prices.json \
        src/test/java/uk/co/redsoft/sandbox/BookPricingRetryIntegrationTest.java
git commit -m "Integration test: verify 5xx from price catalogue dead-letters after 3 retries"
```
