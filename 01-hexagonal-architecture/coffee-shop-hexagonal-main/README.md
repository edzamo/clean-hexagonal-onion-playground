# Coffee Shop - Arquitectura Hexagonal (Spring Boot)

Microservicio de pedidos de una cafetería: crear, modificar, pagar, preparar, entregar y consultar el recibo.
Proyecto de práctica de arquitectura hexagonal con TDD (ver `CLAUDE.md` para reglas y decisiones).

## Stack
Java 17, Spring Boot 3.5, Spring Data JPA, Bean Validation, MySQL, Gradle. Tests: JUnit 5, AssertJ, Mockito, ArchUnit, H2.

## Estructura de paquetes (`com.ezamora.coffeeshop`)
```
domain/                      order, payment, exception, enums (sin frameworks)
application/in               puertos de entrada: OrderingCoffee, PreparingCoffee
application/out              puertos de salida: Orders, Payments
application/service          casos de uso: CoffeeShop, CoffeeMachine
infrastructure/adapter/in/web        OrderController, ApiExceptionHandler
infrastructure/adapter/in/web/dto    DTOs de petición/respuesta
infrastructure/adapter/out/persistence   order, payment, common (JPA)
infrastructure/config        DatabaseInitializer (perfil dev)
```

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
