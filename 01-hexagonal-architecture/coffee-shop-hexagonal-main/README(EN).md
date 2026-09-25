# Coffee Shop - Hexagonal Architecture (Spring Boot)

Coffee shop ordering microservice: create, update, pay, prepare, hand over and read the receipt.
A practice project for hexagonal architecture with TDD (see `CLAUDE.md` for rules and decisions; it is written in Spanish).

## Stack
Java 17, Spring Boot 3.5, Spring Data JPA, Bean Validation, MySQL, Gradle. Tests: JUnit 5, AssertJ, Mockito, ArchUnit, H2.

## Package structure (`com.ezamora.coffeeshop`)
```
domain/                      order, payment, exception, enums (framework free)
application/in               input ports: OrderingCoffee, PreparingCoffee
application/out              output ports: Orders, Payments
application/service          use cases: CoffeeShop, CoffeeMachine
infrastructure/adapter/in/web        OrderController, ApiExceptionHandler
infrastructure/adapter/in/web/dto    request/response DTOs
infrastructure/adapter/out/persistence   order, payment, common (JPA)
infrastructure/config        DatabaseInitializer (dev profile)
```

## Run
Needs MySQL (`docker-compose -f docker-compose-mysql.yml up -d`).
```bash
# Development: creates tables, shows SQL and loads sample data
gradle bootRun --args='--spring.profiles.active=dev'
```
Without the `dev` profile the configuration is strict (`ddl-auto: validate`): the schema must already exist
(`src/main/resources/db/coffee_shop.sql`). The server listens on port 9000.

## Tests
```bash
gradle test            # test profile: in-memory H2, no MySQL needed
```

## Endpoints
| Method | Path | Description |
|---|---|---|
| POST | `/order` | Create an order |
| GET | `/order/{id}` | Get an order |
| PUT | `/order/{id}` | Replace an order (only while payment is pending) |
| DELETE | `/order/{id}` | Cancel (deletes it; only while payment is pending) |
| POST | `/order/{id}/pay` | Pay by card |
| GET | `/order/{id}/receipt` | Receipt |
| PUT | `/order/{id}/prepare/start` | Start preparing |
| PUT | `/order/{id}/prepare/finish` | Finish preparing |
| PUT | `/order/{id}/take` | Hand over |

Errors use `ProblemDetail`: 404 not found, 409 state/concurrency conflict, 422 invalid data.
