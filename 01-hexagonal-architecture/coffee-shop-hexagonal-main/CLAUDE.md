# Proyecto: coffee-shop-hexagonal

Microservicio de pedidos de una cafetería (ordenar, pagar, preparar, entregar).

## Stack
- Lenguaje: Java 17 (toolchain de Gradle, `JavaLanguageVersion.of(17)`)
- Framework: Spring Boot 3.5.4 (MVC), Spring Data JPA, Bean Validation
- BD: MySQL (`localhost:3306/coffee-shop`); H2 en memoria solo en tests
- Build: Gradle. Tests: `gradle test` (el wrapper `gradlew` no está versionado; sin red usar `gradle test --offline`)
- Tests: JUnit 5, AssertJ, Mockito (`spring-boot-starter-test`), ArchUnit (`archunit-junit5`), H2 (runtime de test)
- Lombok disponible, pero **prohibido** en `domain` y `application`

## Arquitectura
- Estilo: hexagonal. Paquete base: `com.ezamora.coffeeshop`
- Capas:
  - `domain` (por agregado/concepto: `order`, `payment`, `exception`, `enums`), sin frameworks
  - `application/in` (puertos de entrada), `application/out` (puertos de salida), `application/service` (casos de uso con `@Service`; `@Transactional` solo en `CoffeeShop.payOrder`, únicas anotaciones de Spring permitidas ahí)
  - `infrastructure/adapter/in/web` (`OrderController`, `ApiExceptionHandler`) y `infrastructure/adapter/in/web/dto` (DTOs)
  - `infrastructure/adapter/out/persistence` (`order`, `payment`, `common`)
  - `infrastructure/config` (solo `DatabaseInitializer`), `infrastructure/error`
- Puertos: `OrderingCoffee`, `PreparingCoffee` (in); `Orders`, `Payments` (out)
- El `Clock` es un `@Bean` de `CoffeeShopMainApplication`; no hay clase de cableado aparte.
- Los tests de arquitectura (`ArchitectureTest`) protegen estas reglas.

## Convenciones propias
- Idioma del código: inglés. Comentarios y docs: español.
- Entidades JPA con sufijo `JpaEntity` (no homónimas al dominio).
- Errores → `ProblemDetail` centralizado en `ApiExceptionHandler`: OrderNotFound/PaymentNotFound 404; OrderStateException, conflictos de versión o unicidad 409; InvalidOrder/InvalidCard/validación de DTOs 422; el resto 500 sin detalles.
- Commits: Conventional Commits.

## Decisiones de diseño
- Estados persistidos = estados de dominio (PAYMENT_EXPECTED, PAID, PREPARING, READY, TAKEN). CANCELLED no existe: cancelar borra la orden. Migración de datos: `src/main/resources/db/migration_v2_order_status_and_payments.sql`.
- `PaymentJpaEntity` referencia la orden por `order_uuid` (sin asociación JPA) para que `persistence.payment` no dependa de `persistence.order`; la FK vive en el esquema SQL. `UUIDConverter` vive en `persistence.common`.
- Transacciones (INV-18): solo `CoffeeShop.payOrder` (escribe pago y orden) lleva `@Transactional`; ni clase ni `readOnly`. `CoffeeMachine` no es transaccional. Test de rollback: `PayOrderTransactionTest`.
- `OrderJpaEntity` tiene `@Version` (bloqueo optimista). Una fila corrupta (sin ítems) lanza `PersistenceDataCorruptedException` (500).
- Perfiles: base segura (`ddl-auto: validate`, `show-sql: false`); `dev` (`--spring.profiles.active=dev`: update, SQL visible y `DatabaseInitializer`, que carga el seed y hace fallar el arranque si el seed falla); `test` (H2, `application-test.yml`).
- Validación de DTOs: quantity 1..99, items <= 50, titular <= 255, PAN `\d{12,19}`, año 2000..2100, mes 1..12.
- Endpoints: `POST /order`, `GET|PUT|DELETE /order/{id}`, `POST /order/{id}/pay`, `GET /order/{id}/receipt`, `PUT /order/{id}/prepare/start`, `PUT /order/{id}/prepare/finish`, `PUT /order/{id}/take`.

## Deuda conocida (no la resuelvas salvo que se pida)
- **SEC-001 (falta de autenticación) y SEC-002 (falta de propiedad del pedido): EXCEPCIÓN EXPLÍCITA AUTORIZADA POR EL USUARIO.** Cualquier cliente puede operar sobre cualquier pedido conociendo su UUID.
- SEC-004, SEC-007, SEC-009, SEC-010: hallazgos de seguridad de la revisión que siguen sin resolver (endurecimiento pendiente).
- PII: el nombre del titular se guarda en claro en la tabla `payments`.
- INV-09: las excepciones de Spring Data/bloqueo optimista atraviesan `application`; las traduce `ApiExceptionHandler`.
- `Order.create` genera el UUID en el dominio sin un puerto (aceptado).
- `OrderingCoffee` reúne 7 casos de uso; `placeOrder`/`updateOrder` reciben `Order` como comando (aceptado).
- Sin Flyway/Liquibase: el esquema depende del script SQL y de la migración manual.
- JaCoCo no está configurado (no se mide cobertura).
- `setup-hexagonal.sh` está obsoleto (no refleja la estructura actual).
- `.claude/` está ignorado por git (decisión del usuario).

## Cómo trabajar aquí
Sigue el flujo de `.claude/CLAUDE.md` (arquitectura → andamiaje → TDD desde el dominio → revisión).
Ejecuta `gradle test` antes y después de cada ciclo.
