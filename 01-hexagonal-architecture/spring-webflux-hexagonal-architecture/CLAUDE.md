# Contexto del proyecto: Spring WebFlux Hexagonal Architecture (playground de aprendizaje)

## Propósito

Este módulo es un **proyecto de aprendizaje** de arquitectura hexagonal (puertos y
adaptadores) sobre Spring WebFlux (reactivo). El usuario está actuando como
estudiante y pide que Claude actúe como **arquitecto de soluciones + profesor
experto en arquitectura hexagonal**: explicar el "por qué" de cada decisión, no
solo escribir código.

## Estrategia de dos ramas (A/B de dominios)

Se están construyendo **dos features en paralelo, en ramas separadas**, aplicando
las mismas capas hexagonales a dos dominios de negocio distintos:

- `feature/salud-hexagonal` — dominio de citas médicas (appointment/salud).
- `feature/coffee-shop-hexagonal` — dominio de pedidos de café (coffee shop, esta rama).

**Por qué separadas:** es un experimento didáctico. Cada feature avanza de forma
incremental por capas (dominio → puertos → application services → adaptadores),
aplicando la misma lección a ambos dominios para forzar la generalización de los
principios en lugar de memorizar una solución atada a un dominio concreto. Al
final del aprendizaje, se decide **cuál de las dos arquitecturas quedó mejor
diseñada** y esa es la que se promueve/mergea a `main`. La otra queda como
material de referencia/comparación.

**Ambas ramas ya llegaron al mismo nivel de madurez (2026-09-13)**: dominio con
comportamiento real, application (port/in con UseCase+Command, port/out,
service con `@RequiredArgsConstructor`), infrastructure (controller reactivo,
adapter de persistencia in-memory, `ProblemDetail`, Bean Validation, OpenAPI).
Todo probado end-to-end con `curl` en ambas.

## Capas del dominio (convención en este proyecto)

```
src/main/java/com/example/hexagonal/architecture/<bounded-context>/   (salud | coffeeshop)
  domain/
    <aggregate>/        → entidades, value objects, excepciones de dominio, reglas de negocio puras
  application/
    port/in/            → casos de uso (interfaces "UseCase" + su Command si aplica)
    port/out/            → puertos de salida (persistencia)
    service/             → implementación de los casos de uso (@RequiredArgsConstructor, Lombok)
  infrastructure/
    adapter/in/web/       → controller reactivo + DTOs (@Valid) + ProblemDetail handler
    adapter/out/persistence/ → adapter de persistencia (in-memory por ahora, sufijo "Adapter")
    config/                → @Configuration de infraestructura (OpenApiConfig)
```

**Nota**: `application` no repite el nombre del agregado (no es
`application/order/port/in`, es `application/port/in` directo) porque cada
bounded context tiene un solo agregado hoy. `domain` sí lo mantiene
(`domain/order`), por si crece con más agregados.

## Estado actual — `coffeeshop` (dominio: pedidos de café)

- **Dominio** (`domain/order`): `Order` (convertido de `record` anémico a
  clase con comportamiento — ver lección abajo), `LineItem`, `Drink`, `Milk`,
  `Size`, `Location` (Value Objects/enums), `Status` enum
  (`PAYMENT_EXPECTED → PAID → PREPARING → READY → TAKEN`),
  `OrderNotFoundException`, `InvalidOrderTransitionException`.
- **`application/port/in`**: `PlaceOrderUseCase`+`PlaceOrderCommand`,
  `PayOrderUseCase`, `PrepareOrderUseCase`, `MarkOrderReadyUseCase`,
  `TakeOrderUseCase`, `FindOrderUseCase` — cada uno mapea 1:1 a una
  transición real de `Order` (`pay()`, `prepare()`, `markReady()`, `take()`)
  o a la creación/consulta.
- **`application/port/out`**: `LoadOrderPort`, `SaveOrderPort`.
- **`application/service`**: 6 implementaciones, mismo patrón que `salud`
  (`loadPort.loadById → switchIfEmpty(error si no existe) → map(transición
  de dominio) → flatMap(savePort::save)`; `PlaceOrderService` no carga nada,
  crea un `Order` nuevo).
- **`infrastructure`**: `OrderController` (8 endpoints REST), DTOs
  (`PlaceOrderRequest`+`LineItemRequest` con `@Valid`, `OrderResponse`),
  `GlobalExceptionHandler` (`ProblemDetail`: `OrderNotFoundException`→404,
  `InvalidOrderTransitionException`→409, `IllegalArgumentException`/
  `WebExchangeBindException`→400), `OpenApiConfig` (springdoc, Swagger UI en
  `/swagger-ui.html`).
- **`application.yml`** reemplaza `application.properties`.

### Persistencia real (2026-09-13) — misma decisión que `salud`

`InMemoryOrderPersistenceAdapter` reemplazado por `OrderPersistenceAdapter`
con H2 vía R2DBC (mismo stack que `salud`, sin Docker):
`persistence/entity/OrderEntity`, `persistence/mapper/OrderPersistenceMapper`,
`SpringDataOrderRepository`, `schema.sql`.

**Decisión de modelado propia de este dominio**: `Order.items` es una
`List<LineItem>` — `LineItem` es un Value Object sin identidad propia
(nunca se consulta independientemente, vive y muere con el `Order`). En vez
de modelar una tabla hija (`order_line_items`) con joins — que R2DBC no
cascada automáticamente como JPA, habría que orquestarlo a mano —, se
serializa la lista completa a **JSON en una sola columna** (`items`) vía
Jackson, y se deserializa de vuelta al cargar. Es un patrón aceptado en DDD
para colecciones de Value Objects totalmente contenidas dentro del límite
del agregado (el agregado se persiste/carga como una unidad).

**Bug real encontrado y corregido al probar con `curl`:** no había ningún
bean `ObjectMapper` de Jackson disponible para inyectar en este scaffold
(usa `spring-boot-starter-webclient`, no la combinación estándar que
autoconfigura Jackson por WebFlux) — `UnsatisfiedDependencyException` al
arrancar. Fix: el mapper crea su propio `new ObjectMapper()` en vez de
depender de inyección, ya que no se necesita ninguna configuración especial
de serialización para este caso.

Mismos dos bugs de `salud` (case-sensitivity de H2 con identificadores no
citados, e `insert` vs `update` decidido por `R2dbcEntityTemplate` en vez de
la heurística por defecto de `ReactiveCrudRepository.save()`) se evitaron
directamente esta vez porque ya se conocían — aplicados desde el inicio acá.

### Operadores de error de Reactor + Testing (2026-09-13) — mismo patrón que `salud`

`OrderPersistenceAdapter.save()` agrega `.doOnError` (logging, `@Slf4j`) +
`.onErrorMap(DataAccessException.class, ex -> new OrderPersistenceException(...))`
(`application/port/out`, agnóstico de tecnología) → `503` en
`GlobalExceptionHandler`. `retryWhen`/`onErrorResume` no se usan por la misma
razón que en `salud`: no hay ninguna llamada `WebClient` a un servicio
externo en este proyecto todavía.

**Testing**, misma pirámide: `OrderTest` (dominio, JUnit puro, ciclo de vida
completo + transiciones inválidas), `PlaceOrderServiceTest`/
`PayOrderServiceTest` (Mockito + `StepVerifier`, casos crear/mutar-encontrado/
mutar-no-encontrado), `OrderControllerTest` (`@WebFluxTest` +
`@MockitoBean`, verifica vacío→404 y `@Valid` rechazando items vacíos),
`OrderEndToEndTest` (`@SpringBootTest(RANDOM_PORT)` +
`@AutoConfigureWebTestClient`, flujo completo contra la BD H2 real). Todo en
verde a la primera corrida — las incompatibilidades de paquete de Spring Boot
4/Jackson 3 (`@WebFluxTest`, `@MockitoBean`, `@AutoConfigureWebTestClient`,
`tools.jackson.databind.JsonNode`) ya se conocían de `salud`, así que se
usaron los imports correctos desde el inicio.

### Lección real encontrada al portear desde `salud` (2026-09-13)

`Order` originalmente era un **`record` anémico** (solo datos: `id`,
`location`, `items`, `status`), sin ningún método de transición — a
diferencia de `Appointment` en `salud`, que ya tenía su ciclo de vida
completo como métodos de la entidad. Antes de poder definir los `UseCase` de
`port/in` (que deben mapear 1:1 a una operación real del dominio, no
inventarse), hubo que **primero** convertir `Order` de `record` a clase
mutable con métodos `pay()`/`prepare()`/`markReady()`/`take()` y sus guard
clauses — igual que `Appointment`. Motivo de record→class: `Order` tiene
identidad (`id`) y un ciclo de vida mutable en el tiempo (`Status`), que es
la definición de **Entidad** en DDD (identidad + ciclo de vida) — un
`record` es idiomático para **Value Objects** (sin identidad, igualdad
estructural), no para una entidad con estado cambiante.

## Buenas prácticas Java 21 y Spring Boot — mismas que `salud`

Ver el detalle completo (con citas a documentación oficial de Spring,
`buckpal`, Vaughn Vernon, etc.) en la memoria de este proyecto — resumen:
constructor injection con Lombok `@RequiredArgsConstructor` (nunca
`@Autowired` en campos), `ProblemDetail` (RFC 7807) en vez de DTOs de error
caseros, excepciones de dominio con nombre propio (no
`IllegalStateException`/`IllegalArgumentException` genéricas para reglas de
negocio), Bean Validation en el borde HTTP + self-validating value objects en
los Command, OpenAPI desde el arranque, `application.yml` en vez de
`.properties`.

## Cómo colaborar en este proyecto

- Explicar el razonamiento arquitectónico detrás de cada capa/decisión (rol de
  profesor), no solo generar código.
- No mezclar features entre ramas sin que el usuario lo pida explícitamente.
- Antes de cambiar de rama, verificar que no haya trabajo sin commitear que
  se mezclaría con el árbol de trabajo de la otra rama.
- Mantener la separación estricta `port/in` vs `port/out` como el estándar a
  seguir en ambas features.
- Cada vez que se agregue un puerto/caso de uso nuevo, verificar que ya
  exista el método de dominio correspondiente — si no existe, el dominio va
  primero.
