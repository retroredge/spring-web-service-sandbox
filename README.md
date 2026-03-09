# Spring Web Service Sandbox

A sandbox for experimenting with Spring Boot Web service features. Built with Java 25 (LTS) and Spring Boot 4.0.3.

Features:

* JSON REST API
* Async CSV bulk import via RabbitMQ - intra-service queue-based load leveling pattern
* Spring Data JPA persistence to a MySQL DB
* Spring actuator for health, metrics, and operational endpoints
* Flyway DB migration
* OpenAPI docs with Swagger UI
* SLF4J/Logback debug logging
* Docker cluster for MySQL and RabbitMQ
* Multi-layer testing: Mockito, Spring and Testcontainers (Dockerised dependencies in tests)
* Prometheus metrics with Grafana dashboards

## Architecture

This project uses the **Ports and Adapters** (Hexagonal) architecture pattern. The codebase is organised into two top-level packages:

### `domain`

The core of the application — no framework or infrastructure dependencies.

| Package | Contents |
|---------|----------|
| `domain.model` | Domain objects (e.g. `Book`) |
| `domain.ports.in` | Incoming port interfaces — use cases the domain exposes to the outside world (e.g. `BookUseCase`) |
| `domain.ports.out` | Outgoing port interfaces — abstractions the domain requires of infrastructure (e.g. `BookStore`, `BookImportPort`) |
| `domain.usecase` | Implementations of the incoming ports (e.g. `BookService`) |

### `adapters`

Adapters connect the domain to the outside world. They implement or call the port interfaces.

| Package | Contents |
|---------|----------|
| `adapters.in.web` | HTTP driving adapters — Spring MVC controllers and request DTOs |
| `adapters.in.messaging` | AMQP driving adapters — RabbitMQ listeners and message types |
| `adapters.out.persistence` | JPA driven adapters — Spring Data repositories and entities |
| `adapters.out.messaging` | AMQP driven adapters — RabbitMQ publishers |

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


### Prometheus UI

| URL | Description |
|-----|-------------|
| `http://localhost:9090` | Prometheus UI |
| `http://localhost:9090/targets` | Scrape target status — sandbox job should show UP |
| `http://localhost:9090/query` | Query explorer |


 Useful queries:

| Query | Description |
|-------|-------------|
| `books_import_listener_seconds_count` | Total number of messages processed |
| `books_import_listener_seconds_sum` | Total time spent processing |
| `books_import_listener_seconds_max` | Slowest single message |
| `rate(books_import_listener_seconds_count[1m])` | Messages per second over the last minute |

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
| `GET http://localhost:8080/books/{id}` | Get a book by ID |
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

## Sequence Diagram for the import books journey

The key thing the diagram makes visible is the async boundary — the client gets its 202 Accepted as soon as all valid 
rows are enqueued, before any of them have actually been persisted. The RabbitMQ → listener → DB flow happens entirely 
independently afterwards.

[Import books Mermaid Diagram](https://mermaid.live/edit#pako:eNqVVd1u2jAUfpUjX1EtUAh_xReVGOvFpnVry9qLiRuTHMBqYme2Q8uqSnuIPeGeZI5DgEBgXaRI9sn3fef4_DgvJJAhEko0_khRBPiBs7li8USAe1hgpIJRxFGYwpYwZXjAEyYMvJfycSSFUTKKUFUhHljEQ2ZVjtHvNY6YRmDabceoljzAY-iPcSKVubFvRrhj0yk3mf0mnUZcL4YhS0x1IDn2-va08meuDQo8Gu3YnmQT6x0mUnNrWZ3w-ylhZexEFLA8r_XLy3IaKdx8HX-D86m16nPu4oLaaPwAMx7h2ZYfSZnAzBYIWbCADKDkU_Exe8q6FY4EPsFIITOYx2ibQJuaVbGuMAr12Um1TW0pLPMl1lSuUSJucPWKEJZcRsxwKfT2YK71IrPzDYQ0gHFiVruYN50xkvPGE1MC3oF-5Ml-jjCyzefC_2_lcvZGMo6ZCIsMVGXwqOp6CCjk1c5MtSDXqxRY49fs7VBQQGG9p3iSvcVbgWIuKARSLFGZoQjHaM9xe391f-VBlZD9XGzdstjsnc2q5y1OwW_6MAwCtFOyg_8iDYK0TjfT6R2OIoWhXolgoaSQqYY_v37DgiUJ2qZgMzt0TptrUGhSJXbly_Ph8hJCjFqzOe4ep3BeSufWvRTXOad2UOyD-Shz96sbOH5lbQ6r6q4aCpotjzMcpow38koYblb_5BxcTGtf6Ogl2gG0XvaZLXOvbwrQtXeFmy1hP3HZ5li2jlRtl2KblHhkrnhIqFEpeiRGFbNsS14y0ISYBcY4IdQuQ6YeJ2QiXi3HXuHfpYwLmpLpfEHojNlLwyNpkl1561_mxqqsN1QjmQpDaLvfbzoVQl_IM6H1VrPbaQwuWheD3qDda_u-R1bW3Gv1G62e73da3U6n2-91Xz3y0zn2G81-u-V32gO_e9Fp9vsewTArwnX-53Y_8Ne_PKeKTA)
