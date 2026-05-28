# Price Repository replaceAll Design

**Date:** 2026-05-28

## Problem

`PriceLookupService` calls `priceStore.deleteByIsbn(isbn)` followed by `priceStore.saveAll(prices)` as two separate, non-transactional port calls. If the process dies between the two operations, prices for that ISBN are deleted and never restored. There is also no transaction boundary ensuring atomicity.

## Solution

Replace the two port methods with a single `replaceAll(String isbn, List<BookPrice> prices)` method that expresses the domain intent — atomically replace all prices for an ISBN. The adapter implements it `@Transactional`, keeping the infrastructure concern out of the domain layer.

## Components

### `PriceRepositoryPort`

Remove `saveAll` and `deleteByIsbn`. Add:

```java
void replaceAll(String isbn, List<BookPrice> prices);
```

### `BookPriceRepositoryAdapter`

Remove `saveAll` and `deleteByIsbn` methods. Add:

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

`JpaBookPriceRepository.deleteByIsbn` is retained — it is a JPA repository method, not a port method.

### `PriceLookupService`

Replace:
```java
priceStore.deleteByIsbn(isbn);
priceStore.saveAll(prices);
```

With:
```java
priceStore.replaceAll(isbn, prices);
```

## Testing

### `PriceLookupServiceTest` (unit)

Update mock expectation: replace `deleteByIsbn` + `saveAll` verifications with a single `replaceAll` verification.

### `BookPriceRepositoryAdapterTest` (unit)

Replace separate `saveAll` and `deleteByIsbn` tests with a `replaceAll` test. Verify it delegates to `jpaBookPriceRepository.deleteByIsbn` then `jpaBookPriceRepository.saveAll` in that order using `InOrder`.

### `JpaBookPriceRepositoryIntegrationTest` (integration)

Add a test that pre-populates prices for an ISBN, calls `replaceAll` with a different set of prices, then asserts only the new prices exist for that ISBN — confirming atomicity and completeness of the replace operation.
