# Spring Boot Clean Architecture — proyecto de aprendizaje

Segundo módulo del playground de arquitecturas (el primero es
[`01-hexagonal-architecture`](../../01-hexagonal-architecture)). Implementa
**Clean Architecture** (Robert C. Martin) con **Spring MVC + Virtual Threads +
JPA/Hibernate** — deliberadamente distinto del stack 100% reactivo del
proyecto hexagonal, para diversificar el aprendizaje real.

Dominio: banco — cuentas (`Account`) y transferencias, con reglas de negocio
reales (saldo insuficiente, cuentas cerradas).

> Contexto detallado (decisiones, contrastes con el proyecto hexagonal) en
> [`CLAUDE.md`](./CLAUDE.md).

## Estructura (círculos concéntricos de Clean Architecture)

```
com.example.cleanarchitecture.bank/
  entities/              → Account, Money, Transaction (reglas de negocio puras)
  usecases/
    port/in/               interfaces de caso de uso (Input Boundary) + Command
    port/out/              Gateway (equivalente al "Port" de hexagonal)
    interactor/            implementaciones (Interactor, equivalente a "Service")
  interfaceadapters/
    controller/             HTTP ↔ Command/Response
    presenter/              Response Model ↔ View Model (patrón completo, 1 caso de uso)
    gateway/                implementaciones JPA de los Gateway
  frameworks/
    config/                 OpenAPI
    web/                    manejo global de errores (ProblemDetail)
```

Documentación y API interactiva: `http://localhost:8080/swagger-ui.html`.

## Cómo correr la app

```bash
./gradlew bootRun
```

Ejemplo de flujo completo vía HTTP:

```bash
# Abrir dos cuentas
curl -X POST http://localhost:8080/accounts -H "Content-Type: application/json" -d '{"holderName":"Ana"}'
curl -X POST http://localhost:8080/accounts -H "Content-Type: application/json" -d '{"holderName":"Beto"}'

# Depositar
curl -X POST http://localhost:8080/accounts/{id}/deposit -H "Content-Type: application/json" -d '{"amount":100.00}'

# Consultar balance (usa el patrón Presenter/ViewModel completo)
curl http://localhost:8080/accounts/{id}/balance

# Transferir entre dos cuentas
curl -X POST http://localhost:8080/accounts/{id}/transfer -H "Content-Type: application/json" -d '{"targetAccountId":"...","amount":40.00}'

# Retirar
curl -X POST http://localhost:8080/accounts/{id}/withdraw -H "Content-Type: application/json" -d '{"amount":10.00}'
```

## Referencias

- **Robert C. Martin ("Uncle Bob")** — *Clean Architecture* (2017) —
  Entities, Use Cases (Interactor + Input/Output Boundary), Interface
  Adapters, Frameworks & Drivers; el "Dependency Rule".
- **Vaughn Vernon** — *Implementing Domain-Driven Design* (IDDD) — Entidad vs
  Value Object, aplicado también a por qué `Transaction` es un `record`.
- **Documentación oficial de Spring** — Virtual Threads
  (`spring.threads.virtual.enabled`), Spring Data JPA, Bean Validation,
  `ProblemDetail` (RFC 7807).

## Decisiones de diseño registradas

Ver `CLAUDE.md` para el detalle completo, entre ellas: por qué se usó
MVC+Virtual Threads+JPA en vez de WebFlux+R2DBC, la tabla de equivalencia de
terminología con el proyecto hexagonal (`Gateway` vs `Port`, `Interactor` vs
`Service`), por qué solo `GetAccountBalance` implementa el patrón completo
Input/Output Boundary + Presenter, por qué `TransferInteractor` orquesta dos
agregados, y las incompatibilidades reales de Spring Boot 4.1.1 encontradas
al testear (`@WebMvcTest`/`TestRestTemplate` aún no existen como artefactos
separados en esta versión).
