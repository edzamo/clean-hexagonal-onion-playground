# Clean / Hexagonal / Onion Architecture Playground

Playground de aprendizaje para comparar, en la práctica y no solo en teoría,
tres estilos de arquitectura por capas sobre **Spring Boot + Java 21**:
**Hexagonal (Ports & Adapters)**, **Clean Architecture** y **Onion
Architecture**. Cada una vive en su propia carpeta numerada, con su propio
dominio de negocio, su propio README y su propio `CLAUDE.md` con las
decisiones de diseño registradas.

## Proyectos

| # | Arquitectura | Proyecto | Dominio | Stack |
|---|---|---|---|---|
| 01 | **Hexagonal** (Ports & Adapters) | [`spring-webflux-hexagonal-architecture`](01-hexagonal-architecture/spring-webflux-hexagonal-architecture) | Citas médicas (`salud`) | Spring WebFlux (reactivo) + R2DBC/H2 |
| 01 | Hexagonal (referencia externa) | [`coffee-shop-hexagonal-main`](01-hexagonal-architecture/coffee-shop-hexagonal-main) | Pedidos de café | Spring MVC + JPA/MySQL — POC generado con IA (Copilot/Gemini) basado en un artículo externo, ver su propio README |
| 02 | **Onion Architecture** | [`spring-boot-onion-architecture`](02-onion-architecture/spring-boot-onion-architecture) | Inventario (`Product`/`StockItem`) | Spring MVC + Virtual Threads + JPA/H2 |
| 03 | **Clean Architecture** | [`spring-boot-clean-architecture`](03-clean-architecture/spring-boot-clean-architecture) | Banco (cuentas y transferencias) | Spring MVC + Virtual Threads + JPA/H2 |

Cada proyecto documenta en su propio `README.md` cómo correrlo y en su propio
`CLAUDE.md` el razonamiento detrás de cada decisión (por qué esa capa, por
qué ese nombre, qué bug real se encontró al probarlo).

## Comparación entre las tres arquitecturas

La tabla comparativa completa, los diagramas de cada estilo y la guía de
"cuándo usar cada una" están en:

📄 **[`docs/architecture-notes-and-diagrams/comparacion-arquitecturas.md`](docs/architecture-notes-and-diagrams/comparacion-arquitecturas.md)**

## Estrategia de aprendizaje

- **01-hexagonal-architecture** se construyó con una estrategia A/B: dos
  dominios (`salud` vs `coffee-shop`) implementados en paralelo en ramas
  separadas hasta la misma madurez, y se promovió a `main` el que quedó mejor
  diseñado (`salud`, rama `feature/salud-hexagonal`). `coffee-shop-hexagonal`
  quedó documentado y disponible como material de comparación en
  `feature/coffee-shop-hexagonal`.
- **02-onion-architecture** y **03-clean-architecture** se construyeron cada
  uno como una única implementación de referencia, reutilizando
  deliberadamente el mismo stack (Spring MVC + Virtual Threads + JPA) entre
  sí para aislar la variable que sí cambia: el estilo arquitectónico, no la
  tecnología.
- Todo puerto/caso de uso en un proyecto reactivo (WebFlux) debe exponerse
  como `Mono`/`Flux`, nunca con retorno bloqueante — ver el contrato reactivo
  documentado en el `CLAUDE.md` de `01-hexagonal-architecture`.

## Referencias generales

Cada proyecto documenta sus propias referencias en su README (sección
"Referencias"). Como resumen, las fuentes principales usadas en todo el
playground:

- Alistair Cockburn — [*Hexagonal Architecture*](https://alistair.cockburn.us/hexagonal-architecture/) (artículo original).
- Tom Hombergs — [*Get Your Hands Dirty on Clean Architecture*](https://reflectoring.io/book/) y [`buckpal`](https://github.com/thombergs/buckpal).
- Jeffrey Palermo — [*The Onion Architecture*](https://jeffreypalermo.com/2008/07/the-onion-architecture-part-1/) (2008).
- Robert C. Martin ("Uncle Bob") — *Clean Architecture* (2017).
- Vaughn Vernon — *Implementing Domain-Driven Design* (IDDD).
- Eric Evans — *Domain-Driven Design*.
- Arho Huttunen — [arhohuttunen.com/hexagonal-architecture-spring-boot](https://www.arhohuttunen.com/hexagonal-architecture-spring-boot/) (base de `coffee-shop-hexagonal-main`).
- Documentación oficial de Spring Framework / Spring Boot y Project Reactor.
