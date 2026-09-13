# Spring WebFlux Hexagonal Architecture — proyecto de aprendizaje

Playground para aprender **Arquitectura Hexagonal (Puertos y Adaptadores)** con
**Spring WebFlux** (programación reactiva) y **Java 21**, comparando dos
dominios de negocio implementados en paralelo en ramas separadas de git:

- `feature/salud-hexagonal` — dominio de citas médicas (rama activa).
- `feature/coffee-shop-hexagonal` — dominio de pedidos de café.

Al final del aprendizaje se decide cuál de las dos implementaciones queda
mejor diseñada, y esa es la que se promueve a `main`.

> Para el contexto detallado de cómo se está desarrollando esto (decisiones,
> convenciones, estado por rama), ver [`CLAUDE.md`](./CLAUDE.md) en la raíz de
> este módulo.

## Dominio activo: `salud` (citas médicas)

```
salud/
  domain/appointment/        → Appointment (entidad), Value Objects, excepciones de dominio, reglas de negocio
  application/
    port/in/                 → casos de uso (interfaces *UseCase* + sus Command)
    port/out/                → puertos de salida (persistencia)
    service/                 → implementación de los casos de uso (@RequiredArgsConstructor)
  infrastructure/
    adapter/in/web/            → AppointmentController (REST reactivo) + DTOs (@Valid) + ProblemDetail handler
    adapter/out/persistence/   → InMemoryAppointmentPersistenceAdapter (in-memory por ahora)
    config/                    → @Configuration de infraestructura (hoy: OpenApiConfig)
```

Documentación y API interactiva: `http://localhost:8080/swagger-ui.html`
(spec en `/v3/api-docs`).

Ciclo de vida de una cita: `REQUESTED → CONFIRMED → IN_PROGRESS → COMPLETED`,
con salidas posibles a `CANCELLED` o `NO_SHOW`.

## Cómo correr la app

```bash
./gradlew bootRun
```

Ejemplo de flujo completo vía HTTP:

```bash
# Crear una cita
curl -X POST http://localhost:8080/appointments \
  -H "Content-Type: application/json" \
  -d '{"patientId":"<uuid>","practitionerId":"<uuid>","start":"2026-10-01T10:00:00","end":"2026-10-01T10:30:00","reason":"Chequeo general"}'

# Confirmar / iniciar / completar / cancelar / reprogramar / no-show
curl -X POST http://localhost:8080/appointments/{id}/confirm
curl -X POST http://localhost:8080/appointments/{id}/start
curl -X POST http://localhost:8080/appointments/{id}/complete
curl -X POST http://localhost:8080/appointments/{id}/cancel -d '{"reasonDescription":"..."}'
curl -X POST http://localhost:8080/appointments/{id}/reschedule -d '{"start":"...","end":"..."}'
curl -X POST http://localhost:8080/appointments/{id}/no-show

# Consultar
curl http://localhost:8080/appointments/{id}
```

## Referencias usadas en este aprendizaje

- **Alistair Cockburn** — [Hexagonal Architecture](https://alistair.cockburn.us/hexagonal-architecture/)
  (el artículo original que define Ports & Adapters: *Application Core*,
  *Ports*, *Adapters*; no define "casos de uso" como bloque obligatorio, solo
  como técnica de especificación).
- **Tom Hombergs** — [*Get Your Hands Dirty on Clean Architecture*](https://reflectoring.io/book/)
  y su proyecto de referencia [`buckpal`](https://github.com/thombergs/buckpal)
  en GitHub — la implementación práctica de Hexagonal en Spring más citada de
  la industria; de ahí tomamos la convención `application/port/in`
  (`XxxUseCase` + `XxxCommand`), `application/port/out`, `application/service`,
  y el uso de `@RequiredArgsConstructor` (Lombok) para constructor injection.
- **Vaughn Vernon** — *Implementing Domain-Driven Design* (IDDD) — patrón de
  *self-validating value objects* (por qué la validación estructural de un
  Command/VO vive en su propio constructor, distinta de una regla de negocio).
- **Eric Evans** — *Domain-Driven Design* — Entidades, Value Objects,
  Application Services como conceptos base de DDD táctico.
- **Documentación oficial de Spring Framework** — [Dependency Injection](https://docs.spring.io/spring-framework/reference/core/beans/dependencies/factory-collaborators.html) —
  recomendación oficial de constructor injection sobre field/setter injection
  con `@Autowired`.
- **Project Reactor** — [`Mono`/`Flux` reference docs](https://projectreactor.io/docs/core/release/reference/)
  — contrato reactivo obligatorio en `port/in`, `port/out` y adapters.

## Decisiones de diseño registradas

Ver `CLAUDE.md` para el detalle completo y su justificación (con fecha), entre
ellas:
- Por qué `application` no repite el nombre del agregado (`application/port/in`,
  no `application/appointment/port/in`), pero `domain` sí lo mantiene.
- Por qué los `Command` son `record`, no interfaces (interfaz = comportamiento
  sustituible; record = datos inmutables).
- Por qué se mantiene el sufijo `UseCase` en los puertos de entrada.
- Por qué `FindAppointmentUseCase` puede devolver vacío pero el adapter web
  debe traducir ese vacío a `404` explícitamente (WebFlux no lo hace solo).
