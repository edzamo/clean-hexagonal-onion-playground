# Contexto del proyecto: Spring Boot Onion Architecture (playground de aprendizaje)

## Propósito

Tercer módulo del playground de arquitecturas (`01-hexagonal-architecture`,
`03-clean-architecture` son los otros dos). Implementa **Onion Architecture**
(Jeffrey Palermo, 2008) con Spring MVC + Virtual Threads + JPA/Hibernate
(mismo stack que `03-clean-architecture`, elegido a propósito para no volver
a pelear con las incompatibilidades de artefactos de Spring Boot 4.1.1 que ya
se resolvieron ahí — ver más abajo). Dominio: inventario (`Product` + `StockItem`).

## Fuentes/referencias usadas para este proyecto

- Palermo, J. — [*The Onion Architecture*](https://jeffreypalermo.com/2008/07/the-onion-architecture-part-1/)
  (2008), el artículo original que define el patrón.
- El proyecto que el usuario agregó como referencia,
  [`01-hexagonal-architecture/coffee-shop-hexagonal-main`](../../01-hexagonal-architecture/coffee-shop-hexagonal-main)
  (basado a su vez en [arhohuttunen.com/hexagonal-architecture-spring-boot](https://www.arhohuttunen.com/hexagonal-architecture-spring-boot/)) —
  de ahí se tomó la convención de que las interfaces de repositorio vivan en
  `domain/repository` (no en `application`), que también se usa acá.
- Mismas referencias ya citadas en `01-hexagonal-architecture` y
  `03-clean-architecture` (Vaughn Vernon, documentación oficial de Spring).

## Convención de paquetes — Onion (anillos concéntricos)

```
com.example.onionarchitecture.inventory/
  domain/
    model/        → Product, StockItem (Entidades), ProductId (VO), excepciones
    service/       → InventoryDomainService — lógica de negocio que cruza
                      agregados, sin I/O, SIN anotaciones de framework
    repository/    → interfaces de persistencia (ProductRepository, StockRepository)
                      — en Onion viven en domain, no en application
  application/
    dto/            → Command (RegisterProductCommand, etc.) y resultados (StockLevel)
    service/         → Application Services (orquestan domain + repository + @Transactional)
  infrastructure/
    web/              → controller + DTOs (@Valid) + manejador global de errores
    persistence/       → implementaciones JPA de los repository
      entity/           → @Entity de JPA, distintas del dominio
      mapper/            → Entity JPA ↔ Entidad de dominio
    config/            → @Configuration (OpenAPI, y el bean del Domain Service)
```

## Lo que hace a Onion distinto de Hexagonal/Clean (en este playground)

| | Hexagonal (`01-`) | Clean (`03-`) | Onion (acá) |
|---|---|---|---|
| Interfaz de "caso de uso" por operación | Sí (`port/in`, sufijo `UseCase`) | Sí (`Input Boundary`) | **No** — Application Service es una clase concreta directa, sin interfaz por operación. Onion no exige ese nivel de segregación; su única regla dura es "las dependencias apuntan hacia adentro" a nivel de capa. |
| Dónde viven las interfaces de persistencia | `application/port/out` | `usecases/port/out` (Gateway) | **`domain/repository`** — Onion las trata como parte del dominio (qué necesita el negocio), no de la aplicación. |
| Lógica que cruza más de un agregado | Vive en el Interactor/Service de aplicación | Vive en el Interactor (`TransferInteractor`) | **Domain Service** (`InventoryDomainService`) — capa de dominio separada, explícitamente sin anotaciones de Spring, para que siga siendo 100% pura y testeable sin ningún framework. |
| Cómo se registra esa pieza pura como bean de Spring | N/A | N/A | `@Configuration` externo (`DomainServiceConfig`) con un `@Bean` — el objeto de dominio nunca importa Spring. |

Ninguna de las tres es "más correcta" — Onion es, de las tres, la que más
insiste en que el *dominio* (no solo los casos de uso) puede tener sus
propios servicios, separados de la orquestación de aplicación.

## Por qué `InventoryDomainService` no lleva `@Service` de Spring

A diferencia de todos los `Interactor`/`Service` de los otros dos proyectos
(anotados `@Service` directamente), acá el Domain Service es una clase Java
simple — cero imports de Spring. Se registra como bean desde
`infrastructure/config/DomainServiceConfig` (`@Configuration` + `@Bean`).
Esto es intencional y es la regla más estricta de Onion: el círculo de
dominio no puede depender de nada externo, ni siquiera de las anotaciones
del framework que lo va a hospedar.

## Por qué `reserveStock`/`discontinueProduct` necesitan un Domain Service

`Product` y `StockItem` son **dos agregados independientes** (mismo patrón
que `TransferInteractor` en `03-clean-architecture` con dos `Account`). La
regla "no se puede reservar stock de un producto discontinuado" depende del
estado de AMBOS agregados a la vez — no cabe dentro de un solo método de
`Product` ni de `StockItem` sin que uno dependa del otro. En Hexagonal/Clean
esa lógica se hubiera escrito directamente en el Service/Interactor de
aplicación; en Onion, como esa lógica es **puramente de negocio** (no
involucra I/O, transacciones, ni orquestación), se extrae a un Domain
Service explícito — la aplicación solo carga los agregados, delega la regla,
y guarda el resultado.

## Decisión de stack: mismo que `03-clean-architecture` (Spring MVC + Virtual Threads + JPA)

No se repitió la diversificación hacia WebFlux/R2DBC acá a propósito — ya se
demostró ese contraste en `01-` vs `03-`. Reusar el stack conocido evitó
repetir la investigación de artefactos faltantes de Spring Boot 4.1.1.

## Incompatibilidades de Spring Boot 4.1.1 (ya conocidas de `03-clean-architecture`)

Aplicadas desde el inicio, sin tener que redescubrirlas:
- `@WebMvcTest` no existe todavía como artefacto separado → `MockMvc`
  standalone (`MockMvcBuilders.standaloneSetup(controller)`).
- `TestRestTemplate` tampoco existe → `java.net.http.HttpClient` del JDK
  contra `@LocalServerPort`.
- Jackson 3: `tools.jackson.databind.JsonNode`/`ObjectMapper`, no
  `com.fasterxml.jackson`.

## Testing — 14 tests, todos en verde a la primera corrida

`StockItemTest` (dominio, JUnit puro) · `InventoryDomainServiceTest` (Domain
Service puro, **sin Spring, sin mocks** — la prueba más importante de este
proyecto, porque demuestra que la regla de negocio central es 100%
independiente de cualquier framework) · `ReserveStockServiceTest` (Mockito
para los repository, pero el Domain Service real, no mockeado — no tiene
sentido fingir una regla de negocio pura) · `InventoryControllerTest`
(`MockMvc` standalone) · `InventoryEndToEndTest` (`@SpringBootTest` +
`HttpClient`, contra la BD H2 real, incluyendo la regla cruzada
"reservar de un producto discontinuado" verificada de punta a punta).

## Cómo colaborar en este proyecto

- Mismo criterio que `01-` y `03-`: explicar el razonamiento, no solo
  generar código, y contrastar explícitamente con las otras dos
  arquitecturas del playground.
- Mantener el Domain Service 100% libre de imports de Spring/framework —
  es la regla que más fácil se rompe sin darse cuenta.
- Documentar toda fuente/referencia usada (libros, artículos, proyectos de
  referencia) — el usuario lo pidió explícitamente para este proyecto.
