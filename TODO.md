# TODO

## Test coverage

Create a unit test for BookPriceRepositoryAdapter

Are there any other pure tests that could be added whre we have classes with logic in them (not POJOs)?

## Lombok builders
Prefer Lombok `@Builder` over custom constructors where appropriate.
Current example: `BookPriceEntity(String isbn, String countryCode, BigDecimal price)`
constructs a `BookPriceId` internally — investigate whether `@Builder` can replace this
without making call sites more verbose.
