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
- `feature/coffee-shop-hexagonal` — dominio de pedidos de café (coffee shop).

**Por qué separadas:** es un experimento didáctico. Cada feature avanza de forma
incremental por capas (dominio → puertos → application services → adaptadores),
aplicando la misma lección a ambos dominios para forzar la generalización de los
principios en lugar de memorizar una solución atada a un dominio concreto. Al
final del aprendizaje, se decide **cuál de las dos arquitecturas quedó mejor
diseñada** y esa es la que se promueve/mergea a `main`. La otra queda como
material de referencia/comparación.

**Regla de trabajo:** en un momento dado solo se trabaja activamente **una** rama
(actualmente `feature/salud-hexagonal`). A medida que se construye una capa ahí,
esa misma capa se debe eventualmente "portear" (no copiar 1:1, sino aplicar el
mismo principio) a la otra rama para que ambas terminen con el mismo nivel de
madurez arquitectónica y sean comparables.

## Capas del dominio (convención en este proyecto)

```
src/main/java/com/example/hexagonal/architecture/<bounded-context>/   (salud | coffeeshop)
  domain/
    <aggregate>/        → entidades, value objects, reglas de negocio puras
                           (subcarpeta por agregado; hoy salud solo tiene "appointment").
                           Se mantiene por si el bounded context crece con más de un
                           agregado con ciclo de vida propio.
  application/
    port/in/            → casos de uso (interfaces "UseCase" que expone la aplicación)
    port/out/           → puertos de salida (persistencia, integraciones externas)
    service/            → implementación de los casos de uso (orquesta domain + ports out)
  infrastructure/
    adapter/in/web/      → adaptadores de entrada (controllers REST reactivos)
    adapter/out/...      → adaptadores de salida (repos, clientes externos)
```

**Nota de decisión (2026-09-13):** `application` NO repite el nombre del agregado
(no es `application/appointment/port/in`, es `application/port/in` directo),
porque hoy cada bounded context (`salud`, `coffeeshop`) tiene un solo agregado y
además cada rama de git ya aísla completamente un dominio del otro — repetirlo
sería redundante. Si `salud` llegara a tener un segundo agregado con casos de
uso propios (ej. `Patient` como agregado independiente, no solo un VO), ahí sí
se reintroduce una subcarpeta por agregado en `application`, igual que ya
existe en `domain`. Esto sigue el mismo criterio de la referencia `buckpal`
(github.com/thombergs/buckpal, del libro *Get Your Hands Dirty on Clean
Architecture* de Tom Hombergs), que tampoco repite el nombre del agregado en
`application/port/in|out` porque su proyecto entero es un solo bounded context.

## Estado actual (snapshot — verificar con `git log`/código antes de asumir vigente)

- **`feature/salud-hexagonal`** (rama activa):
  - Dominio completo: `Appointment`, `AppointmentStatus`, `PatientId`,
    `PractitionerId`, `TimeSlot`, `CancellationReason`.
  - Puertos de entrada definidos (`application/appointment/port/in`):
    `RequestAppointmentUseCase`/`Command`, `ConfirmAppointmentUseCase`,
    `StartAppointmentUseCase`, `CompleteAppointmentUseCase`,
    `CancelAppointmentUseCase`/`Command`, `RescheduleAppointmentUseCase`/`Command`,
    `MarkAppointmentNoShowUseCase`, `FindAppointmentUseCase`.
  - Puertos de salida definidos (`application/appointment/port/out`):
    `LoadAppointmentPort`, `SaveAppointmentPort`.
  - `application/service` implementado (2026-09-13): una clase `Service` por
    cada `UseCase` (`RequestAppointmentService`, `ConfirmAppointmentService`,
    `StartAppointmentService`, `CompleteAppointmentService`,
    `CancelAppointmentService`, `RescheduleAppointmentService`,
    `MarkAppointmentNoShowService`, `FindAppointmentService`), anotadas
    `@Service` para que Spring las registre como bean e inyecte el/los
    `port/out` que necesiten por constructor. Patrón usado en cada una que
    muta el agregado (todas menos `Request` y `Find`):
    `loadAppointmentPort.loadById(id) → switchIfEmpty(error si no existe) →
    map(método de dominio) → flatMap(saveAppointmentPort::save)`.
    `RequestAppointmentService` no necesita cargar nada (crea un `Appointment`
    nuevo). `FindAppointmentService` no muta ni valida "no encontrado" —
    una consulta que no encuentra nada es un resultado válido (`Mono.empty()`
    → HTTP 404 más adelante en el adapter), a diferencia de un comando que
    exige que el agregado ya exista.
  - Se agregó `AppointmentNotFoundException` en `domain/appointment` (no en
    `application`) porque representa un hecho de negocio ("no existe tal
    cita"), no un detalle de infraestructura.
  - `infrastructure` implementado (2026-09-13), probado end-to-end levantando
    la app real con `curl` (create → find → confirm → start → complete →
    intento de cancelar ya completada = 409):
    - `adapter/out/persistence/InMemoryAppointmentRepository` (`@Repository`,
      `ConcurrentHashMap`) implementa `LoadAppointmentPort` +
      `SaveAppointmentPort`. Decisión elegida sobre R2DBC/Mongo: empezar
      in-memory (cero dependencias nuevas) para validar todo el cableado
      hexagonal ya; swapear a una BD reactiva real después no debería tocar
      `domain`/`application` — esa sustituibilidad es la demostración del
      valor de hexagonal. Nota: `Appointment` no se autoasigna `id` en su
      constructor "nuevo" (queda `null`); el adapter de persistencia es quien
      genera el `UUID` al guardar (identidad asignada por infraestructura,
      no por el dominio) — válido, pero es una decisión a tener presente.
      Sin dependencias nuevas en `build.gradle` todavía (no hay R2DBC/Mongo).
    - `adapter/in/web/AppointmentController` (`@RestController`,
      `/appointments`) expone los 8 casos de uso vía HTTP, con DTOs propios
      (`RequestAppointmentRequest`, `RescheduleAppointmentRequest`,
      `CancelAppointmentRequest`, `AppointmentResponse`) que traducen
      primitivos JSON ↔ Value Objects de dominio — el controller nunca
      serializa `Appointment` directamente.
    - `adapter/in/web/AppointmentExceptionHandler` (`@RestControllerAdvice`)
      mapea excepciones a HTTP: `AppointmentNotFoundException` → 404,
      `IllegalStateException` (regla de negocio/transición inválida) → 409,
      `IllegalArgumentException` (dato mal formado) → 400.
    - **Lección real encontrada al probar:** `FindAppointmentUseCase` devolver
      `Mono.empty()` cuando no existe la cita es correcto a nivel de
      aplicación (una consulta vacía es un resultado válido), PERO WebFlux por
      defecto traduce un `Mono<T>` vacío a `200 OK` con body vacío, no a `404`.
      Se verificó con `curl` y se corrigió agregando
      `.switchIfEmpty(Mono.error(new AppointmentNotFoundException(id)))` en
      el método `findById` del controller — la traducción "vacío → 404" es
      responsabilidad del adapter de entrada, no del `FindAppointmentService`.
  - **Mejoras de mejores prácticas Spring Boot aplicadas (2026-09-13), probadas
    en vivo con `curl` y `./gradlew test`:**
    - **Constructor injection con Lombok**: los 8 `service` + el
      `AppointmentController` usan `@RequiredArgsConstructor` sobre campos
      `private final` (recomendación oficial de Spring: "the Spring team
      generally advocates constructor injection"; `@Autowired` en campos
      queda descartado). Coincide con cómo lo hace `buckpal`.
    - **`InMemoryAppointmentRepository` renombrado a
      `InMemoryAppointmentPersistenceAdapter`** — alineado con la convención
      de `buckpal` (`AccountPersistenceAdapter`), que sí sufija "Adapter" en
      el lado de persistencia (aunque no en el controller web).
    - **Excepción de dominio propia**: `InvalidAppointmentTransitionException`
      (en `domain/appointment`) reemplaza los `IllegalStateException`
      genéricos que lanzaba `Appointment` en sus guard clauses de transición.
      Motivo: un `IllegalStateException` genérico podría originarse en
      cualquier bug de programación, no solo en la regla de negocio — mapearlo
      a 409 en el handler arriesgaba ocultar errores reales detrás de un
      código que sugiere "regla de negocio violada".
    - **`ProblemDetail` (RFC 7807)**: `AppointmentExceptionHandler` migrado del
      `ErrorResponse` casero a `ProblemDetail` nativo de Spring Boot 3+.
      Mapeo: `AppointmentNotFoundException`→404,
      `InvalidAppointmentTransitionException`→409,
      `IllegalArgumentException`→400, `WebExchangeBindException`
      (fallos de `@Valid`)→400 con detalle de campo (`errors: [...]`).
    - **Bean Validation**: `spring-boot-starter-validation` + `@Valid` en los
      `@RequestBody` (`RequestAppointmentRequest`, `CancelAppointmentRequest`,
      `RescheduleAppointmentRequest`) con `@NotNull`/`@NotBlank` — rechaza
      JSON malformado en el borde HTTP antes de construir el Command. El
      Command sigue validando su propia estructura en el compact constructor
      como capa adicional, no redundante (otros adapters de entrada podrían
      construir el Command sin pasar por `@Valid`).
    - **OpenAPI**: `springdoc-openapi-starter-webflux-ui` +
      `infrastructure/config/OpenApiConfig` (bean `OpenAPI` con metadata).
      Swagger UI en `/swagger-ui.html`, spec en `/v3/api-docs`. Verificado
      levantando la app.
    - **`application.yml`** reemplaza `application.properties` (config
      jerárquica, estándar moderno de Spring Boot) — incluye la config de
      `springdoc`.
    - **`infrastructure/config/`** es la carpeta para `@Configuration`/`@Bean`
      de infraestructura (hoy solo `OpenApiConfig`; ahí irían `@Configuration`
      de R2DBC, CORS, `@ConfigurationProperties` propias, etc. cuando hagan
      falta — no se inventó nada especulativo, solo lo que ya se necesitaba).
    - **Pendiente, no implementado todavía** (documentado para no perderlo):
      entidades de persistencia separadas del dominio (`persistence/entity` +
      mapper) — no aplica aún porque el adapter es in-memory y guarda
      `Appointment` directamente; el patrón (entidad de persistencia distinta
      del modelo de dominio, con su propio mapper) se activa recién cuando se
      swapee a una BD real (R2DBC/Mongo). Tests (unit/Mockito/WebTestClient/
      e2e) — pendiente, siguiente prioridad natural.

- **`feature/coffee-shop-hexagonal`**:
  - Dominio completo: `Order`, `LineItem`, `Drink`, `Milk`, `Size`, `Status`,
    `Location`.
  - Ya tiene `application/service/OrderService` y un adapter de entrada
    (`infrastructure/adapter/in/web/OrderController` + `GlobalExceptionHandler`).
  - Los puertos actuales (`OrderingCoffee`, `PreparingCoffee`) **no** están
    separados explícitamente en `port/in`/`port/out` como en `salud` — esa
    separación es justamente lo que habría que portear desde `salud` cuando
    corresponda.

## Contrato reactivo (obligatorio, Spring WebFlux)

Toda operación de I/O o que cruce un límite de capa (use case en `port/in`,
puerto en `port/out`, método de controller) **debe** devolver `Mono<T>` (0 o 1
resultado) o `Flux<T>` (0..N resultados) de Project Reactor — nunca tipos
bloqueantes (`T`, `List<T>`, `Optional<T>`, `void` síncrono) ni bloquear el hilo
(nada de `.block()` en código de producción). Los **Commands/Value Objects**
(records de datos puros, sin I/O) NO llevan `Mono`/`Flux` — solo las
*operaciones* son reactivas, los *datos* no.

Ejemplo ya aplicado correctamente en `salud`:
- `RequestAppointmentCommand` (record, datos puros) → sin Mono/Flux. Correcto.
- `RequestAppointmentUseCase.request(command)` (operación) → `Mono<Appointment>`. Correcto.
- `LoadAppointmentPort.loadByPatientId(...)` (puede devolver varias citas) → `Flux<Appointment>`. Correcto.

## Por qué cada puerto de `salud` es "in" o "out" (para efectos de aprendizaje)

**`port/in`** = lo que la aplicación **ofrece** hacia afuera (lo invoca un
adapter de entrada, ej. un controller web). Son los casos de uso / intenciones
del negocio:
- `RequestAppointmentUseCase`, `ConfirmAppointmentUseCase`, `StartAppointmentUseCase`,
  `CompleteAppointmentUseCase`, `CancelAppointmentUseCase`,
  `RescheduleAppointmentUseCase`, `MarkAppointmentNoShowUseCase` → cada uno
  representa una transición de estado del ciclo de vida de una cita
  (`AppointmentStatus`). Se modelan como casos de uso separados (en vez de un
  único `AppointmentService` con muchos métodos) para que cada intención de
  negocio sea explícita y testeable de forma aislada (Interface Segregation).
- `FindAppointmentUseCase` → caso de uso de consulta (lectura), separado de los
  de escritura/comando (alineado a un espíritu CQRS ligero).

**`port/out`** = lo que la aplicación **necesita** del mundo exterior
(lo implementa un adapter de salida, ej. un repositorio R2DBC/Mongo). Son
dependencias hacia infraestructura, definidas desde el punto de vista del
dominio (Dependency Inversion: el dominio define el contrato, la
infraestructura lo implementa):
- `SaveAppointmentPort` → persistir una cita (crear/actualizar).
- `LoadAppointmentPort` → recuperar citas (por id, por paciente, por
  profesional) para que los use cases puedan validar reglas de negocio
  (ej. evitar solapamiento de horarios) antes de decidir.

## Qué falta portear a `coffee-shop` (pendiente, no implementado aún)

`feature/coffee-shop-hexagonal` está desactualizada respecto a este nivel de
madurez: `OrderingCoffee` (interfaz vacía), `PreparingCoffee` (clase vacía) y
`OrderService` (clase vacía) no tienen contenido, y no siguen la separación
`port/in`/`port/out`. Cuando se aborde esa rama, el equivalente pedagógico a
portear —aplicando el dominio `Order`/`LineItem`/`Status`, no copiando el de
citas— sería algo como:

- `port/in`: casos de uso por transición de `Status` de una orden (ej.
  `PlaceOrderUseCase`, `PrepareOrderUseCase`, `CompleteOrderUseCase`,
  `CancelOrderUseCase`) + `FindOrderUseCase` para lectura — mismo principio de
  separar comando por intención de negocio y aislar las consultas.
- `port/out`: `SaveOrderPort` (persistir) y `LoadOrderPort` (recuperar por id /
  por ubicación `Location`) — mismo principio de invertir la dependencia hacia
  infraestructura.
- Todo con `Mono`/`Flux`, igual que en `salud`.

Este es un ejemplo de contraste pedagógico útil: `salud` ya modela "aprobar
antes de decidir" (carga datos vía `port/out` antes de mutar), mientras
`coffee-shop` empezó por el borde equivocado (un controller con un endpoint
hardcodeado, sin dominio conectado) — vale la pena señalarlo al usuario como
ejemplo de qué NO hacer primero.

## Interfaces vs. records: dónde aplica cada uno (decisión 2026-09-13)

Regla: **interfaz = contrato de comportamiento con implementaciones intercambiables
(puertos); clase/record = datos que viajan a través de esos contratos.** No todo
tiene que ser interfaz — solo los puertos (`port/in`, `port/out`) lo son. Los
`Command` (ej. `RescheduleAppointmentCommand`) son `record`s inmutables, no
interfaces, porque no hay nada que intercambiar: son un solo paquete de datos
que se le pasa a un método de un `UseCase`.

**Naming de `port/in`:** se decidió mantener el sufijo `UseCase`
(`RescheduleAppointmentUseCase`, no `RescheduleAppointmentPort` ni una interfaz
genérica sin sufijo). Motivo: (a) coincide con la referencia `buckpal`, (b) el
dominio ya tiene un método con el mismo verbo (`Appointment.reschedule(...)`) —
el sufijo evita que el nombre del puerto de aplicación choque/se confunda con
el nombre del método de dominio.

**Validación en el compact constructor de un `Command` (ej.
`RescheduleAppointmentCommand`, `CancelAppointmentCommand`) NO es lógica de
negocio y se queda ahí.** Es validación estructural ("¿el dato llegó completo?"
— null-checks), un patrón de *self-validating value object* (Vaughn Vernon,
IDDD) que también usa `buckpal` en `SendMoneyCommand`. Es distinta de la
validación de reglas de negocio real que SÍ depende del estado del dominio
(ej. `Appointment.reschedule()` valida que el `AppointmentStatus` permita la
transición) — esa sí vive en la entidad de dominio, no en el Command, y ya
está bien ubicada.

## Buenas prácticas Java 21 a seguir en este proyecto

- **Records** para Value Objects y Commands (`PatientId`, `TimeSlot`,
  `RequestAppointmentCommand`, etc.) con validación en el *compact constructor*
  (patrón ya usado en `CancelAppointmentCommand`, `RescheduleAppointmentCommand`).
- **Sealed interfaces / sealed classes** cuando haya una jerarquía cerrada de
  tipos conocida en tiempo de compilación (candidato: modelar
  `CancellationReason` o resultados de dominio como jerarquía sellada si crece).
- **Pattern matching for switch** (con `switch` expressions exhaustivos) para
  manejar `AppointmentStatus`/`Status` en vez de cadenas de `if/else`.
- **Records + pattern matching (deconstruction)** al leer commands/eventos
  compuestos, cuando aplique.
- Evitar `null` como valor de retorno de dominio: usar `Optional<T>` solo en
  fronteras no-reactivas puntuales, pero preferir `Mono.empty()` en las
  fronteras reactivas (ports/use cases).
- Inmutabilidad por defecto: VOs y Commands son records inmutables; las
  entidades (`Appointment`, `Order`) deben exponer transiciones de estado como
  métodos que devuelven una nueva instancia o mutan de forma controlada, nunca
  setters públicos genéricos.

## Cómo colaborar en este proyecto

- Explicar el razonamiento arquitectónico detrás de cada capa/decisión (rol de
  profesor), no solo generar código.
- No mezclar features entre ramas sin que el usuario lo pida explícitamente.
- Al avanzar una capa en la rama activa, señalar qué equivalente falta portear
  en la otra rama, sin implementarlo automáticamente salvo que se solicite.
- Mantener la separación estricta `port/in` vs `port/out` como el estándar a
  seguir en ambas features.
- **Cada vez que se agregue un puerto/caso de uso nuevo**, documentar en este
  archivo (sección correspondiente) por qué es `in` o `out` y qué principio de
  diseño ilustra — no basta con escribir el código, el objetivo es el
  aprendizaje explícito.
- Verificar siempre el contrato reactivo (`Mono`/`Flux`, sin `.block()`) antes
  de dar por terminada una capa, en ambas features.
