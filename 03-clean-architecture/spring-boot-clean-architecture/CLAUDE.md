# Contexto del proyecto: Spring Boot Clean Architecture (playground de aprendizaje)

## Propósito

Segundo módulo del playground de arquitecturas (`01-hexagonal-architecture` fue
el primero). Implementa **Clean Architecture** (Robert C. Martin / "Uncle Bob")
con Spring Boot, un solo dominio (banco: cuentas y transferencias), sin la
estrategia A/B de dos ramas que se usó en hexagonal — acá se construye una sola
implementación de referencia.

## Decisión de stack: Spring MVC + Virtual Threads + JPA (no WebFlux/R2DBC)

A propósito, **distinto** del proyecto `01-hexagonal-architecture` (100%
reactivo). Motivo: diversificar el aprendizaje real en vez de repetir el mismo
stack — Spring MVC bloqueante + Virtual Threads (`spring.threads.virtual.enabled=true`)
es la otra opción real del árbol de decisión "WebFlux vs MVC+VT" que ya
estudiamos, y JPA/Hibernate (vs R2DBC) es el otro extremo del par que
comparamos en el cheat sheet de Spring Boot. Contrastes concretos encontrados:

- **`@Transactional` funciona normal** aquí (JPA es bloqueante, hay
  `ThreadLocal`/sesión) — en `salud`/`coffeeshop` (R2DBC) hubiera hecho falta
  `TransactionalOperator` en su lugar.
- **`Optional<Account>`** en el Gateway (síncrono) en vez de `Mono<Account>`.
- **JPA/Hibernate SÍ detecta correctamente INSERT vs UPDATE** aunque el
  mapper asigne el `UUID` antes de guardar (a diferencia del bug que
  encontramos con `ReactiveCrudRepository.save()` en R2DBC) — `merge()` hace
  un SELECT primero para decidir. Verificado con `curl` real, no en teoría.

## Convención de paquetes — Clean Architecture (círculos concéntricos)

```
com.example.cleanarchitecture.bank/
  entities/              → Enterprise Business Rules (el círculo más interno)
                            Account, Money, Transaction (VOs/Entidad), excepciones de dominio
  usecases/              → Application Business Rules
    port/in/               interfaces de caso de uso ("Input Boundary") + Command
    port/out/               interfaces que el caso de uso necesita ("Gateway", no "Port")
    interactor/             implementaciones ("Interactor", no "Service"/"UseCaseImpl")
  interfaceadapters/      → Interface Adapters
    controller/             adapta HTTP ↔ Command/Response
    presenter/               adapta Response Model ↔ View Model (solo en 1 caso de uso, ver abajo)
    gateway/                implementa los Gateway de usecases/port/out (JPA)
      entity/                clases @Entity de JPA, distintas de entities/ (dominio)
      mapper/                Entity JPA ↔ Entidad de dominio
  frameworks/             → Frameworks & Drivers
    config/                 @Configuration (OpenAPI)
    web/                    @RestControllerAdvice (ProblemDetail)
```

## Terminología: Clean Architecture vs Hexagonal — mismo concepto, otro nombre

| Concepto | Hexagonal (`01-...`) | Clean Architecture (acá) |
|---|---|---|
| Interfaz que expone una capacidad de negocio | `port/in`, sufijo `UseCase` | `usecases/port/in`, también interfaz de "caso de uso" (Uncle Bob lo llama "Input Boundary") |
| Implementación del caso de uso | `application/service`, sufijo `Service` | `usecases/interactor`, sufijo **`Interactor`** (término propio de Uncle Bob) |
| Interfaz hacia infraestructura (persistencia) | `port/out`, sufijo `Port` | `usecases/port/out`, sufijo **`Gateway`** (término propio de Uncle Bob) |
| Adaptador que implementa la interfaz de salida | `infrastructure/adapter/out/persistence`, sufijo `Adapter` | `interfaceadapters/gateway`, sufijo `Impl` |
| Adaptador de entrada (HTTP) | `infrastructure/adapter/in/web` | `interfaceadapters/controller` |

Ningún nombre es "más correcto" — son dos vocabularios distintos para la
misma idea (aislar el negocio de los detalles técnicos). Vale la pena poder
hablar los dos en una entrevista.

## El patrón completo Input/Output Boundary + Presenter (solo en `GetAccountBalance`)

Uncle Bob, en el libro, describe el caso de uso como algo que **no retorna un
valor directamente** — llama a un "Output Boundary" (interfaz), implementado
por un "Presenter" en la capa de adaptadores, que convierte el "Response
Model" en un "View Model" listo para mostrar. Implementamos esto **fielmente,
una sola vez** (`GetAccountBalanceUseCase` → `GetAccountBalanceOutputBoundary`
→ `AccountBalancePresenter` → `AccountBalanceViewModel`), para el resto de
los casos de uso (`Deposit`, `Withdraw`, `Transfer`, `OpenAccount`) usamos el
patrón **simplificado** de retorno directo (el Interactor devuelve la
Entidad/Result, y el Controller mismo actúa como su propio "presenter").

**Por qué no todos con el patrón completo:** la ceremonia completa
(Input Boundary sin retorno + Output Boundary + Presenter con estado
`@RequestScope`) tiene sentido cuando el mismo caso de uso podría alimentar
presentadores distintos (HTTP JSON, CLI, otro formato) sin cambiar el
Interactor. En una API REST típica donde el Controller es el único consumidor,
esa indirección es ceremonia sin beneficio real — la mayoría de
implementaciones modernas de Clean Architecture en Spring simplifican a
retorno directo. Mostrar ambas, una vez cada una, es la lección: **conocer el
patrón completo, y saber cuándo simplificarlo con criterio.**

**Detalle técnico real de la implementación:** `AccountBalancePresenter` es
`@RequestScope` (no singleton) — si fuera singleton y el `Interactor` (que sí
es singleton) lo mantuviera como campo, requests concurrentes pisarían el
estado (`viewModel`) del otro. `@RequestScope` ya incluye
`proxyMode = ScopedProxyMode.TARGET_CLASS` por defecto, así que Spring puede
inyectar el proxy en el Interactor singleton sin problema.

## Por qué `TransferInteractor` es el caso más interesante de este proyecto

A diferencia de **todos** los casos de uso de `salud`/`coffeeshop` (cada uno
tocaba un solo agregado), una transferencia bancaria involucra **dos
agregados `Account` independientes** — el `Interactor` (no un método de
`Account`) es quien orquesta "retirar de uno, depositar en el otro, como una
unidad atómica" (`@Transactional`). Un método de una sola entidad solo puede
proteger invariantes sobre *su propio* estado; coordinar dos agregados es
responsabilidad de la capa de casos de uso, no del dominio.

## `Transaction` — por qué es `record` (inmutable) aunque tenga identidad

Un registro de ledger, una vez creado, **nunca se muta** — es un hecho
histórico inmutable, aunque tenga un `TransactionId`. No todo lo que tiene
identidad necesita ser una clase mutable: la regla real es "¿esta instancia
se muta en memoria después de crearse?" — acá la respuesta es no, así que
`record` es lo correcto (mismo principio que las entidades de persistencia
`AccountJpaEntity`... salvo que esas SÍ son mutables, ver siguiente sección).

## `AccountJpaEntity` — por qué SÍ es una clase mutable (a diferencia de R2DBC)

Contraste real con `AppointmentEntity`/`OrderEntity` (R2DBC, `record`s) en el
proyecto hexagonal: JPA/Hibernate necesita un **constructor sin argumentos**
(protegido) para construir proxies/entidades administradas por la sesión de
persistencia, y tradicionalmente usa clases mutables porque Hibernate hace
*dirty checking* sobre un objeto vivo en memoria durante la transacción — un
`record` no encaja bien con ese modelo (no tiene setters, y aunque JPA puede
mapear records desde Hibernate 6, sigue siendo el patrón menos común/idiomático
en JPA clásico). En R2DBC, sin sesión ni dirty-checking, cada entidad es una
instantánea de una sola pasada — por eso ahí sí conviene `record`. Es el
framework de persistencia el que decide, no una regla universal.

## Bug real de dominio encontrado y corregido (2026-09-13)

`Account.withdraw()`/`close()`/`requireActive()` originalmente incluían
`id.value()` en el mensaje de la excepción — pero una `Account` recién creada
(`new Account(holderName)`, antes de guardarse) tiene `id == null`. Un test
unitario puro (`AccountTest`, sin persistencia de por medio) lo hubiera
disparado como `NullPointerException` en vez de la excepción de negocio
esperada. Fix: los mensajes ya no dependen de `id`, que puede legítimamente
no existir todavía en un objeto de dominio recién construido.

## Incompatibilidades reales de Spring Boot 4.1.1 encontradas (release muy nueva)

- **`@WebMvcTest` no existe todavía como artefacto separado** en esta versión
  (a diferencia de `@WebFluxTest`, que sí tiene su propio módulo
  `spring-boot-webflux-test`) — no hay ningún `spring-boot-webmvc-test` en
  Maven Central a la fecha. Se verificó inspeccionando los `.jar` reales, no
  asumiendo. Fix: `MockMvc` en modo *standalone*
  (`MockMvcBuilders.standaloneSetup(controller)`), sin `@WebMvcTest` ni
  contexto de Spring — más simple, y evita depender de un artefacto que
  todavía no existe.
- **`TestRestTemplate` tampoco existe** en esta versión (no está en ningún
  `.jar` de `4.1.1`). Fix: `java.net.http.HttpClient` del JDK directo contra
  `@LocalServerPort` (que sí existe) — 100% agnóstico de framework de testing,
  cero riesgo de módulo faltante.
- **Jackson 3** (mismo hallazgo que en el proyecto hexagonal): `JsonNode`/
  `ObjectMapper` se importan de `tools.jackson.databind`, no de
  `com.fasterxml.jackson.databind`.

## Testing — 15 tests, todos en verde

`AccountTest` (dominio, JUnit puro) · `TransferInteractorTest`/
`GetAccountBalanceInteractorTest` (Mockito, sin Spring) ·
`AccountControllerTest` (`MockMvc` standalone) · `AccountEndToEndTest`
(`@SpringBootTest(RANDOM_PORT)` + `HttpClient` del JDK, contra la BD H2 real).

## Cómo colaborar en este proyecto

- Explicar el razonamiento arquitectónico (rol de profesor), no solo generar
  código — mismo criterio que `01-hexagonal-architecture`.
- Contrastar explícitamente con las decisiones ya tomadas en hexagonal
  (terminología, mutabilidad, reactivo vs bloqueante) — el valor pedagógico
  de este proyecto está en la comparación, no en repetir lo mismo con otro
  nombre de paquete.
- Verificar siempre con `curl`/tests reales antes de dar una capa por
  terminada — varias de las lecciones de este documento solo se
  descubrieron al ejecutar, no leyendo el código.
