# Price Repository replaceAll Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Replace the non-atomic `deleteByIsbn` + `saveAll` pair in `PriceRepositoryPort` with a single `replaceAll` method that is implemented `@Transactional` in the adapter, fixing a correctness gap where a crash between the two calls would wipe prices.

**Architecture:** `PriceRepositoryPort` (domain port) declares `replaceAll(String isbn, List<BookPrice> prices)`; `BookPriceRepositoryAdapter` (persistence adapter) implements it `@Transactional` using `jpaBookPriceRepository.deleteByIsbn` + `jpaBookPriceRepository.saveAll`; `PriceLookupService` (domain use case) calls the single port method and drops its own `@Transactional`. The old `saveAll` and `deleteByIsbn` port methods are removed entirely.

**Tech Stack:** Java 22, Spring Boot 3.5.9, Spring Data JPA, Mockito, AssertJ, Testcontainers (MySQL), `./mvnw verify`

---

### Task 1: Add `replaceAll` to the port and implement it in the adapter (TDD)

**Files:**
- Modify: `src/test/java/uk/co/redsoft/sandbox/adapters/out/persistence/BookPriceRepositoryAdapterTest.java`
- Modify: `src/main/java/uk/co/redsoft/sandbox/domain/ports/out/PriceRepositoryPort.java`
- Modify: `src/main/java/uk/co/redsoft/sandbox/adapters/out/persistence/BookPriceRepositoryAdapter.java`

- [ ] **Step 1: Write the failing test in `BookPriceRepositoryAdapterTest`**

Add this test to `BookPriceRepositoryAdapterTest`. Also add `import org.mockito.InOrder;` and `import static org.mockito.Mockito.inOrder;` if not already present.

```java
@Test
void replaceAllDeletesThenSavesInOrder() {
    var isbn = "978-0132350884";
    var prices = List.of(
            new BookPrice(isbn, "GBR", new BigDecimal("24.99")),
            new BookPrice(isbn, "USA", new BigDecimal("34.99"))
    );

    adapter.replaceAll(isbn, prices);

    var order = inOrder(jpaBookPriceRepository);
    order.verify(jpaBookPriceRepository).deleteByIsbn(isbn);
    order.verify(jpaBookPriceRepository).saveAll(anyList());
}
```

- [ ] **Step 2: Run test to verify it fails**

```bash
./mvnw test -pl . -Dtest=BookPriceRepositoryAdapterTest#replaceAllDeletesThenSavesInOrder -q
```

Expected: compile error — `replaceAll(String, List)` is not defined on `BookPriceRepositoryAdapter`.

- [ ] **Step 3: Add `replaceAll` to `PriceRepositoryPort`**

The port currently contains `saveAll`, `findByIsbn`, and `deleteByIsbn`. Add `replaceAll` — do NOT remove the existing methods yet (that happens in Task 3).

Replace the entire file content with:

```java
package uk.co.redsoft.sandbox.domain.ports.out;

import uk.co.redsoft.sandbox.domain.model.BookPrice;

import java.util.List;

public interface PriceRepositoryPort {

    void saveAll(List<BookPrice> prices);

    List<BookPrice> findByIsbn(String isbn);

    void deleteByIsbn(String isbn);

    void replaceAll(String isbn, List<BookPrice> prices);
}
```

- [ ] **Step 4: Implement `replaceAll` in `BookPriceRepositoryAdapter`**

Add the following method to `BookPriceRepositoryAdapter`. The existing `saveAll` and `deleteByIsbn` methods stay for now — do not remove them yet. The metrics and `@Timed` annotation move to `replaceAll`; leave the `@Timed` on `saveAll` in place until it is removed in Task 3.

```java
@Timed("books.pricing.db.save")
@Transactional
@Override
public void replaceAll(String isbn, List<BookPrice> prices) {
    jpaBookPriceRepository.deleteByIsbn(isbn);
    var entities = prices.stream()
            .map(p -> new BookPriceEntity(p.isbn(), p.countryCode(), p.price()))
            .toList();
    jpaBookPriceRepository.saveAll(entities);
    DistributionSummary.builder("books.pricing.prices.per.book")
            .register(meterRegistry)
            .record(prices.size());
    meterRegistry.counter("books.pricing.lookup.outcome", "result", "success").increment();
}
```

- [ ] **Step 5: Run test to verify it passes**

```bash
./mvnw test -pl . -Dtest=BookPriceRepositoryAdapterTest#replaceAllDeletesThenSavesInOrder -q
```

Expected: PASS.

- [ ] **Step 6: Run the full test suite**

```bash
./mvnw verify -q
```

Expected: BUILD SUCCESS.

- [ ] **Step 7: Commit**

```bash
git add src/main/java/uk/co/redsoft/sandbox/domain/ports/out/PriceRepositoryPort.java \
        src/main/java/uk/co/redsoft/sandbox/adapters/out/persistence/BookPriceRepositoryAdapter.java \
        src/test/java/uk/co/redsoft/sandbox/adapters/out/persistence/BookPriceRepositoryAdapterTest.java
git commit -m "Add replaceAll to PriceRepositoryPort and implement in adapter"
```

---

### Task 2: Update `PriceLookupService` to use `replaceAll` (TDD)

**Files:**
- Modify: `src/test/java/uk/co/redsoft/sandbox/domain/usecase/PriceLookupServiceTest.java`
- Modify: `src/main/java/uk/co/redsoft/sandbox/domain/usecase/PriceLookupService.java`

- [ ] **Step 1: Update `PriceLookupServiceTest` to expect `replaceAll`**

Replace the entire file with:

```java
package uk.co.redsoft.sandbox.domain.usecase;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.redsoft.sandbox.domain.model.BookPrice;
import uk.co.redsoft.sandbox.domain.ports.out.PriceCataloguePort;
import uk.co.redsoft.sandbox.domain.ports.out.PriceRepositoryPort;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PriceLookupServiceTest {

    @Mock
    private PriceCataloguePort priceCataloguePort;

    @Mock
    private PriceRepositoryPort priceStore;

    @InjectMocks
    private PriceLookupService priceLookupService;

    @Test
    void lookupStoresPricesViaReplaceAll() {
        var isbn = "978-0132350884";
        var prices = List.of(
                new BookPrice(isbn, "GBR", new BigDecimal("24.99")),
                new BookPrice(isbn, "USA", new BigDecimal("34.99"))
        );
        when(priceCataloguePort.fetchPrices(isbn)).thenReturn(prices);

        priceLookupService.lookupAndStorePrices(isbn);

        verify(priceStore).replaceAll(isbn, prices);
    }

    @Test
    void doesNotReplaceWhenCatalogueReturnsEmptyList() {
        var isbn = "978-0132350884";
        when(priceCataloguePort.fetchPrices(isbn)).thenReturn(List.of());

        priceLookupService.lookupAndStorePrices(isbn);

        verify(priceStore, never()).replaceAll(any(), any());
    }

    @Test
    void replacesExistingPricesWhenCalledAgainForSameIsbn() {
        var isbn = "978-0132350884";
        var firstPrices = List.of(new BookPrice(isbn, "GBR", new BigDecimal("24.99")));
        var secondPrices = List.of(
                new BookPrice(isbn, "GBR", new BigDecimal("26.99")),
                new BookPrice(isbn, "EUR", new BigDecimal("28.99"))
        );
        when(priceCataloguePort.fetchPrices(isbn))
                .thenReturn(firstPrices)
                .thenReturn(secondPrices);

        priceLookupService.lookupAndStorePrices(isbn);
        priceLookupService.lookupAndStorePrices(isbn);

        verify(priceStore).replaceAll(isbn, firstPrices);
        verify(priceStore).replaceAll(isbn, secondPrices);
    }
}
```

- [ ] **Step 2: Run tests to verify they fail**

```bash
./mvnw test -pl . -Dtest=PriceLookupServiceTest -q
```

Expected: FAIL — `replaceAll` is not called; service still calls `deleteByIsbn` + `saveAll`.

- [ ] **Step 3: Update `PriceLookupService` to call `replaceAll` and remove `@Transactional`**

Replace the entire file with:

```java
package uk.co.redsoft.sandbox.domain.usecase;

import io.micrometer.core.annotation.Timed;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.co.redsoft.sandbox.domain.ports.in.PriceLookupUseCase;
import uk.co.redsoft.sandbox.domain.ports.out.PriceCataloguePort;
import uk.co.redsoft.sandbox.domain.ports.out.PriceRepositoryPort;

@Slf4j
@RequiredArgsConstructor
@Service
public class PriceLookupService implements PriceLookupUseCase {

    private final PriceCataloguePort priceCataloguePort;
    private final PriceRepositoryPort priceStore;

    @Timed("books.pricing.lookup")
    @Override
    public void lookupAndStorePrices(String isbn) {
        log.debug("Fetching prices for ISBN: {}", isbn);
        var prices = priceCataloguePort.fetchPrices(isbn);
        if (prices.isEmpty()) {
            log.warn("No prices returned for ISBN: {}, skipping update", isbn);
            return;
        }
        priceStore.replaceAll(isbn, prices);
        log.debug("Stored {} price(s) for ISBN: {}", prices.size(), isbn);
    }
}
```

Note: `@Transactional` is removed. The transaction boundary is now in `BookPriceRepositoryAdapter.replaceAll`. The `org.springframework.transaction.annotation.Transactional` import is also removed.

- [ ] **Step 4: Run tests to verify they pass**

```bash
./mvnw test -pl . -Dtest=PriceLookupServiceTest -q
```

Expected: PASS.

- [ ] **Step 5: Run the full test suite**

```bash
./mvnw verify -q
```

Expected: BUILD SUCCESS.

- [ ] **Step 6: Commit**

```bash
git add src/main/java/uk/co/redsoft/sandbox/domain/usecase/PriceLookupService.java \
        src/test/java/uk/co/redsoft/sandbox/domain/usecase/PriceLookupServiceTest.java
git commit -m "Update PriceLookupService to use replaceAll, remove @Transactional from use case"
```

---

### Task 3: Remove `saveAll` and `deleteByIsbn` from port and adapter

**Files:**
- Modify: `src/main/java/uk/co/redsoft/sandbox/domain/ports/out/PriceRepositoryPort.java`
- Modify: `src/main/java/uk/co/redsoft/sandbox/adapters/out/persistence/BookPriceRepositoryAdapter.java`
- Modify: `src/test/java/uk/co/redsoft/sandbox/adapters/out/persistence/BookPriceRepositoryAdapterTest.java`

- [ ] **Step 1: Remove `saveAll` and `deleteByIsbn` from `PriceRepositoryPort`**

Replace the entire file with:

```java
package uk.co.redsoft.sandbox.domain.ports.out;

import uk.co.redsoft.sandbox.domain.model.BookPrice;

import java.util.List;

public interface PriceRepositoryPort {

    List<BookPrice> findByIsbn(String isbn);

    void replaceAll(String isbn, List<BookPrice> prices);
}
```

- [ ] **Step 2: Remove `saveAll` and `deleteByIsbn` from `BookPriceRepositoryAdapter`**

Replace the entire file with:

```java
package uk.co.redsoft.sandbox.adapters.out.persistence;

import io.micrometer.core.annotation.Timed;
import io.micrometer.core.instrument.DistributionSummary;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import uk.co.redsoft.sandbox.domain.model.BookPrice;
import uk.co.redsoft.sandbox.domain.ports.out.PriceRepositoryPort;

import java.util.List;

@RequiredArgsConstructor
@Component
public class BookPriceRepositoryAdapter implements PriceRepositoryPort {

    private final JpaBookPriceRepository jpaBookPriceRepository;
    private final MeterRegistry meterRegistry;

    @Timed("books.pricing.db.save")
    @Transactional
    @Override
    public void replaceAll(String isbn, List<BookPrice> prices) {
        jpaBookPriceRepository.deleteByIsbn(isbn);
        var entities = prices.stream()
                .map(p -> new BookPriceEntity(p.isbn(), p.countryCode(), p.price()))
                .toList();
        jpaBookPriceRepository.saveAll(entities);
        DistributionSummary.builder("books.pricing.prices.per.book")
                .register(meterRegistry)
                .record(prices.size());
        meterRegistry.counter("books.pricing.lookup.outcome", "result", "success").increment();
    }

    @Override
    public List<BookPrice> findByIsbn(String isbn) {
        return jpaBookPriceRepository.findByIsbn(isbn).stream()
                .map(e -> new BookPrice(e.getIsbn(), e.getCountryCode(), e.getPrice()))
                .toList();
    }
}
```

- [ ] **Step 3: Remove the old tests from `BookPriceRepositoryAdapterTest`**

Remove the `saveAllMapsDomainPricesToEntitiesAndPersists` and `deleteByIsbnDelegatesToRepository` test methods. The file should contain only `replaceAllDeletesThenSavesInOrder` and `findByIsbnMapsEntitiesToDomainPrices`. Replace the entire file with:

```java
package uk.co.redsoft.sandbox.adapters.out.persistence;

import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.redsoft.sandbox.domain.model.BookPrice;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BookPriceRepositoryAdapterTest {

    @Mock
    private JpaBookPriceRepository jpaBookPriceRepository;

    @Spy
    private SimpleMeterRegistry meterRegistry = new SimpleMeterRegistry();

    @InjectMocks
    private BookPriceRepositoryAdapter adapter;

    @Test
    void replaceAllDeletesThenSavesInOrder() {
        var isbn = "978-0132350884";
        var prices = List.of(
                new BookPrice(isbn, "GBR", new BigDecimal("24.99")),
                new BookPrice(isbn, "USA", new BigDecimal("34.99"))
        );

        adapter.replaceAll(isbn, prices);

        var order = inOrder(jpaBookPriceRepository);
        order.verify(jpaBookPriceRepository).deleteByIsbn(isbn);
        order.verify(jpaBookPriceRepository).saveAll(anyList());
    }

    @Test
    void findByIsbnMapsEntitiesToDomainPrices() {
        var isbn = "978-0132350884";
        when(jpaBookPriceRepository.findByIsbn(isbn)).thenReturn(List.of(
                new BookPriceEntity(isbn, "GBR", new BigDecimal("24.99")),
                new BookPriceEntity(isbn, "USA", new BigDecimal("34.99"))
        ));

        var result = adapter.findByIsbn(isbn);

        assertThat(result).hasSize(2);
        assertThat(result).extracting(BookPrice::countryCode)
                .containsExactlyInAnyOrder("GBR", "USA");
        assertThat(result).extracting(BookPrice::isbn)
                .containsOnly(isbn);
    }
}
```

- [ ] **Step 4: Run the full test suite**

```bash
./mvnw verify -q
```

Expected: BUILD SUCCESS.

- [ ] **Step 5: Commit**

```bash
git add src/main/java/uk/co/redsoft/sandbox/domain/ports/out/PriceRepositoryPort.java \
        src/main/java/uk/co/redsoft/sandbox/adapters/out/persistence/BookPriceRepositoryAdapter.java \
        src/test/java/uk/co/redsoft/sandbox/adapters/out/persistence/BookPriceRepositoryAdapterTest.java
git commit -m "Remove saveAll and deleteByIsbn from PriceRepositoryPort — replaced by replaceAll"
```

---

### Task 4: Add integration test for replace behaviour

**Files:**
- Modify: `src/test/java/uk/co/redsoft/sandbox/adapters/out/persistence/JpaBookPriceRepositoryIntegrationTest.java`

- [ ] **Step 1: Write the failing test**

Add the following test to `JpaBookPriceRepositoryIntegrationTest`. It pre-populates two prices for an ISBN, performs a delete + insert (the operations that `replaceAll` wraps), then asserts only the new price remains.

```java
@Test
void replacingPricesForIsbnLeavesOnlyNewPrices() {
    var isbn = "978-0132350884";
    jpaBookPriceRepository.save(new BookPriceEntity(isbn, "GBR", new BigDecimal("24.99")));
    jpaBookPriceRepository.save(new BookPriceEntity(isbn, "USA", new BigDecimal("34.99")));

    jpaBookPriceRepository.deleteByIsbn(isbn);
    jpaBookPriceRepository.save(new BookPriceEntity(isbn, "EUR", new BigDecimal("27.99")));

    var remaining = jpaBookPriceRepository.findByIsbn(isbn);
    assertThat(remaining).hasSize(1);
    assertThat(remaining).extracting(BookPriceEntity::getCountryCode).containsOnly("EUR");
}
```

- [ ] **Step 2: Run test to verify it passes**

```bash
./mvnw test -pl . -Dtest=JpaBookPriceRepositoryIntegrationTest#replacingPricesForIsbnLeavesOnlyNewPrices -q
```

Expected: PASS.

- [ ] **Step 3: Run the full test suite**

```bash
./mvnw verify -q
```

Expected: BUILD SUCCESS.

- [ ] **Step 4: Commit**

```bash
git add src/test/java/uk/co/redsoft/sandbox/adapters/out/persistence/JpaBookPriceRepositoryIntegrationTest.java
git commit -m "Add integration test verifying replace behaviour at JPA repository level"
```
