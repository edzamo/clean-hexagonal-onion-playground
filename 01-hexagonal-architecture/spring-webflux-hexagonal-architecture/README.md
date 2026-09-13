# Spring WebFlux Hexagonal Architecture — proyecto de aprendizaje

Playground para aprender **Arquitectura Hexagonal (Puertos y Adaptadores)** con
**Spring WebFlux** (programación reactiva) y **Java 21**, comparando dos
dominios de negocio implementados en paralelo en ramas separadas de git:

- `feature/salud-hexagonal` — dominio de citas médicas.
- `feature/coffee-shop-hexagonal` — dominio de pedidos de café (esta rama).

Al final del aprendizaje se decide cuál de las dos implementaciones queda
mejor diseñada, y esa es la que se promueve a `main`.

> Para el contexto detallado (decisiones, convenciones, estado por rama), ver
> [`CLAUDE.md`](./CLAUDE.md) en la raíz de este módulo.

## Dominio activo: `coffeeshop` (pedidos de café)

```
coffeeshop/
  domain/order/               → Order (entidad), LineItem/Drink/Milk/Size/Location (VOs), excepciones
  application/
    port/in/                  → casos de uso (interfaces *UseCase* + PlaceOrderCommand)
    port/out/                 → puertos de salida (persistencia)
    service/                  → implementación de los casos de uso (@RequiredArgsConstructor)
  infrastructure/
    adapter/in/web/             → OrderController (REST reactivo) + DTOs (@Valid) + ProblemDetail handler
    adapter/out/persistence/    → InMemoryOrderPersistenceAdapter (in-memory por ahora)
    config/                     → @Configuration de infraestructura (OpenApiConfig)
```

Ciclo de vida de una orden:
`PAYMENT_EXPECTED → PAID → PREPARING → READY → TAKEN`.

Documentación y API interactiva: `http://localhost:8080/swagger-ui.html`
(spec en `/v3/api-docs`).

## Cómo correr la app

```bash
./gradlew bootRun
```

Ejemplo de flujo completo vía HTTP:

```bash
# Crear una orden
curl -X POST http://localhost:8080/orders \
  -H "Content-Type: application/json" \
  -d '{"location":"TAKE_AWAY","items":[{"drink":"LATTE","milk":"SOY","size":"MEDIUM","quantity":2}]}'

# Pagar / preparar / marcar lista / tomar
curl -X POST http://localhost:8080/orders/{id}/pay
curl -X POST http://localhost:8080/orders/{id}/prepare
curl -X POST http://localhost:8080/orders/{id}/ready
curl -X POST http://localhost:8080/orders/{id}/take

# Consultar
curl http://localhost:8080/orders/{id}
```

## Referencias usadas en este aprendizaje

- **Alistair Cockburn** — [Hexagonal Architecture](https://alistair.cockburn.us/hexagonal-architecture/)
  (artículo original que define Ports & Adapters).
- **Tom Hombergs** — [`buckpal`](https://github.com/thombergs/buckpal) —
  implementación práctica de referencia de Hexagonal en Spring.
- **Vaughn Vernon** — *Implementing Domain-Driven Design* (IDDD) — Entidad
  (identidad + ciclo de vida) vs Value Object (sin identidad); de ahí la
  decisión de convertir `Order` de `record` a clase.
- **Eric Evans** — *Domain-Driven Design*.
- **Documentación oficial de Spring Framework** — [Dependency Injection](https://docs.spring.io/spring-framework/reference/core/beans/dependencies/factory-collaborators.html).
- **Project Reactor** — [`Mono`/`Flux` reference docs](https://projectreactor.io/docs/core/release/reference/).

## Decisiones de diseño registradas

Ver `CLAUDE.md` para el detalle completo, entre ellas: por qué `Order` pasó
de `record` a clase (Entidad vs Value Object), por qué `application` no
repite el nombre del agregado, por qué los `Command` son `record` y no
interfaces, y el mapeo de excepciones a `ProblemDetail`/HTTP.
