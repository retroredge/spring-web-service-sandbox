# Spring Web Service Sandbox

A sandbox for experimenting with Spring Boot Web service features. Built with Java 22 and Spring Boot 3.5.9.

Features:

* JSON REST API
* Async CSV bulk import via RabbitMQ - intra-service queue-based load leveling pattern
* Async book price lookup — on ingestion, ISBNs are queued and prices fetched from a 3rd-party catalogue API
* Spring Data JPA persistence to a MySQL DB
* Spring actuator for health, metrics, and operational endpoints
* Flyway DB migration
* OpenAPI docs with Swagger UI
* SLF4J/Logback debug logging
* Docker cluster for MySQL and RabbitMQ
* Multi-layer testing: Mockito, Spring and Testcontainers (Dockerised dependencies in tests)
* WireMock (via Testcontainers) for stubbing the 3rd-party price catalogue API in tests
* Prometheus metrics with Grafana dashboards

## Architecture

This project uses the **Ports and Adapters** (Hexagonal) architecture pattern. The codebase is organised into two top-level packages:

### `domain`

The core of the application — no framework or infrastructure dependencies.

| Package | Contents |
|---------|----------|
| `domain.model` | Domain objects (e.g. `Book`, `BookPrice`, `BookDetail`) |
| `domain.ports.in` | Incoming port interfaces — use cases the domain exposes to the outside world (e.g. `BookUseCase`, `BookDetailUseCase`, `PriceLookupUseCase`) |
| `domain.ports.out` | Outgoing port interfaces — abstractions the domain requires of infrastructure (e.g. `BookStore`, `BookImportPort`, `BookPricingPublishPort`, `PriceCataloguePort`, `PriceStore`) |
| `domain.usecase` | Implementations of the incoming ports (e.g. `BookService`, `BookDetailService`, `PriceLookupService`) |

### `adapters`

Adapters connect the domain to the outside world. They implement or call the port interfaces.

| Package | Contents |
|---------|----------|
| `adapters.in.web` | HTTP driving adapters — Spring MVC controllers and request DTOs |
| `adapters.in.messaging` | AMQP driving adapters — RabbitMQ listeners |
| `adapters.out.persistence` | JPA driven adapters — Spring Data repositories and entities |
| `adapters.out.messaging` | AMQP driven adapters — RabbitMQ publishers |
| `adapters.out.http` | HTTP driven adapters — declarative `@HttpExchange` client for the 3rd-party price catalogue API |

### `config`

Spring `@Configuration` classes that wire adapters to ports (dependency injection glue). These sit outside both `domain` and `adapters` as they are cross-cutting infrastructure.

---

## Build and test

```bash
./mvnw clean package
```

## Dependencies via Docker

* MySQL
* RabbitMQ
* WireMock — stubs the 3rd-party price catalogue API for local development
* Prometheus
* Grafana

Start the stack:

```bash
docker compose up -d
```

Stop them when done:

```bash
docker compose down
```

### MySQL

To open a MySQL shell inside the running container:

```bash
docker compose exec mysql mysql -u sandbox -p sandbox sandbox
```

### RabbitMQ UI

The RabbitMQ management UI is available at `http://localhost:15672` (guest/guest).

### WireMock

WireMock stubs the price catalogue API so that the full pricing flow works locally without a real 3rd-party API. Stub mappings are loaded from `src/test/resources/wiremock/mappings/` — the same files used by Testcontainers in integration tests.

| URL | Description |
|-----|-------------|
| `http://localhost:8081/__admin/mappings` | View all loaded stub mappings |
| `http://localhost:8081/catalogue/books/{isbn}/prices` | Call a stub directly (requires Basic Auth: `catalogue-user` / `secret`) |


### Prometheus UI

| URL | Description |
|-----|-------------|
| `http://localhost:9090` | Prometheus UI |
| `http://localhost:9090/targets` | Scrape target status — sandbox job should show UP |
| `http://localhost:9090/query` | Query explorer |


 Useful queries:

| Query | Description |
|-------|-------------|
| `books_import_listener_seconds_count` | Total number of import messages processed |
| `books_import_listener_seconds_sum` | Total time spent processing import messages |
| `books_import_listener_seconds_max` | Slowest single import message |
| `rate(books_import_listener_seconds_count[1m])` | Import messages per second over the last minute |
| `books_pricing_listener_seconds_count` | Total number of pricing messages processed |
| `books_pricing_listener_seconds_sum` | Total time spent fetching and storing prices |
| `books_pricing_listener_seconds_max` | Slowest single pricing message |
| `rate(books_pricing_listener_seconds_count[1m])` | Pricing messages per second over the last minute |

### Grafana

Grafana is available at `http://localhost:3000`. Anonymous access is enabled — no login required.

Prometheus is pre-configured as the default datasource — no manual setup required.

### Connecting to MySQL

```sql
SHOW TABLES;

INSERT INTO books (title, author, isbn, genre) 
VALUES ('The Pragmatic Programmer', 'David Thomas & Andrew Hunt', '978-0135957059', 'Software Engineering');

SELECT * FROM books;
```

## Run local

```bash
./mvnw spring-boot:run -Dspring-boot.run.profiles=local
```

## Bulk import

The `POST /books/import` endpoint accepts a CSV file with a header row and data rows:

```
title,author,isbn,genre
Domain-Driven Design,Eric Evans,978-0321125217,Software Architecture
Clean Code,Robert C. Martin,978-0132350884,Software Engineering
```

Importing a CSV:

```bash
# 10 books
curl -X POST http://localhost:8080/books/import -F "file=@src/test/resources/books-10.csv"

# 10,000 books
curl -X POST http://localhost:8080/books/import -F "file=@src/test/resources/books-10k.csv"
```

## Endpoints

### API

| URL | Description |
|-----|-------------|
| `GET http://localhost:8080/books` | List all books |
| `GET http://localhost:8080/books/{id}` | Get a book by ID, including its GBR price if available |
| `POST http://localhost:8080/books` | Add a new book |
| `POST http://localhost:8080/books/import` | Bulk import books from a CSV file (async via RabbitMQ) |
| `http://localhost:8080/swagger-ui.html` | Swagger UI |
| `http://localhost:8080/v3/api-docs` | OpenAPI JSON spec |

### Actuator 

| URL | Description |
|-----|-------------|
| `http://localhost:8080/actuator/health` | Application health status |
| `http://localhost:8080/actuator/metrics` | Application metrics |
| `http://localhost:8080/actuator/info` | Application info |
| `http://localhost:8080/actuator/env` | Environment properties and config values |
| `http://localhost:8080/actuator/loggers` | View and change log levels at runtime |
| `http://localhost:8080/actuator/mappings` | All registered request mappings |
| `http://localhost:8080/actuator/scheduledtasks` | All scheduled tasks |
| `http://localhost:8080/actuator/httpexchanges` | Recent HTTP request/response history |
| `http://localhost:8080/actuator/beans` | All Spring beans in the application context |
| `http://localhost:8080/actuator/prometheus` | Prometheus metrics scrape endpoint |

## Book pricing

When a book is created (via `POST /books` or CSV import), its ISBN is automatically published to an internal RabbitMQ queue (`books.pricing`). A listener picks this up and calls a 3rd-party price catalogue API to retrieve prices for multiple countries. The prices are stored in the `book_prices` table.

```
POST /books → save to DB → publish ISBN to books.pricing queue
                                    ↓
                        BookPricingListener picks up ISBN
                                    ↓
                        GET /catalogue/books/{isbn}/prices  (3rd-party API)
                                    ↓
                        Store prices in book_prices table
```

The `GET /books/{id}` endpoint includes the GBR price in its response (null if no price has been stored yet):

```json
{
  "id": 1,
  "title": "Clean Code",
  "author": "Robert C. Martin",
  "isbn": "978-0132350884",
  "genre": "Software Engineering",
  "gbrPrice": 24.99
}
```

The price catalogue API requires HTTP Basic Auth. Configure credentials in `application.yaml` (or via environment variables):

```yaml
pricing:
  catalogue:
    base-url: https://your-catalogue-api
    username: your-username
    password: your-password
```

### MySQL

Price data is stored in a separate `book_prices` table with a composite primary key of `(isbn, country_code)`:

```sql
SELECT * FROM book_prices WHERE isbn = '978-0132350884';
```

## Sequence Diagram for the import books journey

The key thing the diagram makes visible is the async boundary — the client gets its 202 Accepted as soon as all valid 
rows are enqueued, before any of them have actually been persisted. The RabbitMQ → listener → DB flow happens entirely 
independently afterwards.

```mermaid
sequenceDiagram
      actor Client
      participant BookController
      participant Validator
      participant BookUseCase as BookService
      participant BookImportPort as RabbitBookPublishAdapter
      participant RabbitMQ
      participant BookImportListener
      participant BookStore as BookRepositoryAdapter
      participant JpaBookRepository

      Client->>BookController: POST /books/import (CSV file)

      loop for each CSV row
          BookController->>BookController: new CreateBookRequest(row fields)
          BookController->>Validator: validate(request)
          Validator-->>BookController: violations

          alt violations not empty
              BookController->>BookController: log.warn + skip row
          else valid
              BookController->>BookController: new CreateBookCommand(request fields)
              BookController->>BookUseCase: importBook(command)
              BookUseCase->>BookImportPort: enqueue(command)
              BookImportPort->>RabbitMQ: convertAndSend(QUEUE, command)
          end
      end

      BookController-->>Client: 202 Accepted

      Note over RabbitMQ,BookImportListener: Asynchronous — happens after 202 is returned

      loop for each queued message
          RabbitMQ->>BookImportListener: onMessage(CreateBookCommand)
          BookImportListener->>BookUseCase: create(command)
          BookUseCase->>BookStore: save(command)
          BookStore->>BookStore: toEntity(command)
          BookStore->>JpaBookRepository: save(entity)
          JpaBookRepository-->>BookStore: BookEntity
          BookStore->>BookStore: toBook(entity)
          BookStore-->>BookUseCase: Book
          BookUseCase-->>BookImportListener: Book
      end
```

[Edit diagram on mermaid.live](https://mermaid.live/edit#pako:eNqVVd1u2jAUfpUjX1EtUAh_xReVGOvFpnVry9qLiRuTHMBqYme2Q8uqSnuIPeGeZI5DgEBgXaRI9sn3fef4_DgvJJAhEko0_khRBPiBs7li8USAe1hgpIJRxFGYwpYwZXjAEyYMvJfycSSFUTKKUFUhHljEQ2ZVjtHvNY6YRmDabceoljzAY-iPcSKVubFvRrhj0yk3mf0mnUZcL4YhS0x1IDn2-va08meuDQo8Gu3YnmQT6x0mUnNrWZ3w-ylhZexEFLA8r_XLy3IaKdx8HX-D86m16nPu4oLaaPwAMx7h2ZYfSZnAzBYIWbCADKDkU_Exe8q6FY4EPsFIITOYx2ibQJuaVbGuMAr12Um1TW0pLPMl1lSuUSJucPWKEJZcRsxwKfT2YK71IrPzDYQ0gHFiVruYN50xkvPGE1MC3oF-5Ml-jjCyzefC_2_lcvZGMo6ZCIsMVGXwqOp6CCjk1c5MtSDXqxRY49fs7VBQQGG9p3iSvcVbgWIuKARSLFGZoQjHaM9xe391f-VBlZD9XGzdstjsnc2q5y1OwW_6MAwCtFOyg_8iDYK0TjfT6R2OIoWhXolgoaSQqYY_v37DgiUJ2qZgMzt0TptrUGhSJXbly_Ph8hJCjFqzOe4ep3BeSufWvRTXOad2UOyD-Shz96sbOH5lbQ6r6q4aCpotjzMcpow38koYblb_5BxcTGtf6Ogl2gG0XvaZLXOvbwrQtXeFmy1hP3HZ5li2jlRtl2KblHhkrnhIqFEpeiRGFbNsS14y0ISYBcY4IdQuQ6YeJ2QiXi3HXuHfpYwLmpLpfEHojNlLwyNpkl1561_mxqqsN1QjmQpDaLvfbzoVQl_IM6H1VrPbaQwuWheD3qDda_u-R1bW3Gv1G62e73da3U6n2-91Xz3y0zn2G81-u-V32gO_e9Fp9vsewTArwnX-53Y_8Ne_PKeKTA)
