# Coffee Shop - Arquitectura Hexagonal (Spring Boot)

Microservicio de pedidos de una cafetería: crear, modificar, pagar, preparar, entregar y consultar el recibo.
Proyecto de práctica de arquitectura hexagonal con TDD (ver `CLAUDE.md` para reglas y decisiones).

## Stack
Java 17, Spring Boot 3.5, Spring Data JPA, Bean Validation, MySQL, Gradle. Tests: JUnit 5, AssertJ, Mockito, ArchUnit, H2.

## Estructura de paquetes (`com.ezamora.coffeeshop`)
```
domain/                                  núcleo puro, sin frameworks
├── order/                               Order (agregado), LineItem
├── payment/                             Payment, CreditCard, Receipt
├── enums/                               Drink, Milk, Size, Location, Status
└── exception/                           InvalidOrderException, OrderStateException, InvalidCardException

application/                             casos de uso y puertos
├── in/                                  puertos de entrada: OrderingCoffee, PreparingCoffee
├── out/                                 puertos de salida: Orders, Payments (+ OrderNotFound, PaymentNotFound)
└── service/                             implementan los puertos de entrada: CoffeeShop, CoffeeMachine

infrastructure/                          adaptadores y configuración
├── adapter/in/web/                      OrderController, ApiExceptionHandler (ProblemDetail)
│   └── dto/                             *Request / *Response (el dominio no se expone)
├── adapter/out/persistence/             implementan Orders y Payments con JPA
│   ├── order/                           OrderServiceAdapter, OrderRepository, OrderMapper
│   │   └── entity/                      OrderJpaEntity, OrderItemJpaEntity, enums *Jpa/OrderStatus/OrderLocation
│   ├── payment/                         PaymentServiceAdapter, PaymentRepository, PaymentMapper, PaymentJpaEntity
│   └── common/                          UUIDConverter
├── config/                              DatabaseInitializer (perfil dev)
└── error/                               PersistenceDataCorruptedException

CoffeeShopMainApplication                punto de arranque Spring Boot
```

Regla de dependencias: `infrastructure → application → domain`. Nunca al revés; lo verifica
`architecture/ArchitectureTest` (ArchUnit). Los adaptadores de persistencia mapean entre entidades JPA y
dominio con `OrderMapper`/`PaymentMapper`, así que el dominio no conoce JPA.

Recursos (`src/main/resources`): `application.yml`, `application-dev.yml`, `openapi.yaml`, `db/coffee_shop.sql`
y `db/migration_v2_order_status_and_payments.sql`.

Tests (`src/test`): unitarios de dominio y servicios con `InMemoryOrders`/`InMemoryPayments`,
tests de contrato compartidos (`contract/OrdersContract`, `PaymentsContract`) que validan tanto los
in-memory como los adaptadores reales, tests de adaptadores web/persistencia, `ArchitectureTest` y
tests de extremo a extremo.

## Ejecutar
Requiere MySQL (`docker-compose -f docker-compose-mysql.yml up -d`).
```bash
# Desarrollo: crea tablas, muestra SQL y carga datos de ejemplo
gradle bootRun --args='--spring.profiles.active=dev'
```
Sin el perfil `dev` la configuración es estricta (`ddl-auto: validate`): el esquema debe existir
(`src/main/resources/db/coffee_shop.sql`). El servidor escucha en el puerto 9000.

## Tests
```bash
gradle test            # perfil test: H2 en memoria, no necesita MySQL
```

## Endpoints
| Método | Ruta | Descripción |
|---|---|---|
| POST | `/order` | Crear pedido |
| GET | `/order/{id}` | Consultar pedido |
| PUT | `/order/{id}` | Reemplazar pedido (solo pendiente de pago) |
| DELETE | `/order/{id}` | Cancelar pedido (lo elimina; solo pendiente de pago) |
| POST | `/order/{id}/pay` | Pagar con tarjeta |
| GET | `/order/{id}/receipt` | Recibo |
| PUT | `/order/{id}/prepare/start` | Empezar a preparar |
| PUT | `/order/{id}/prepare/finish` | Terminar de preparar |
| PUT | `/order/{id}/take` | Entregar |

Errores en formato `ProblemDetail`: 404 no encontrado, 409 conflicto de estado/concurrencia, 422 datos inválidos.

## Documentación
`CLAUDE.md` describe las convenciones, decisiones y la deuda técnica conocida.
Referencia de enfoque: [arhohuttunen.com/hexagonal-architecture-spring-boot](https://www.arhohuttunen.com/hexagonal-architecture-spring-boot/).
