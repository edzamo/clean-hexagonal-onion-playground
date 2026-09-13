# Spring Boot Onion Architecture — proyecto de aprendizaje

Tercer módulo del playground de arquitecturas (los otros dos son
[`01-hexagonal-architecture`](../../01-hexagonal-architecture) y
[`03-clean-architecture`](../../03-clean-architecture)). Implementa **Onion
Architecture** (Jeffrey Palermo) con Spring MVC + Virtual Threads +
JPA/Hibernate. Dominio: inventario — productos y stock.

> Contexto detallado (decisiones, contraste con los otros dos proyectos) en
> [`CLAUDE.md`](./CLAUDE.md).

## Estructura (anillos concéntricos de Onion)

```
com.example.onionarchitecture.inventory/
  domain/
    model/         → Product, StockItem, excepciones de negocio
    service/        → InventoryDomainService (regla que cruza Product + StockItem,
                        sin Spring, sin I/O)
    repository/     → interfaces de persistencia (viven en domain, no en application)
  application/
    dto/             → Command / resultados
    service/          → Application Services (@Transactional, orquestan domain + repos)
  infrastructure/
    web/              → controller + DTOs + manejo de errores (ProblemDetail)
    persistence/       → implementación JPA de los repository
    config/            → OpenAPI + registro del Domain Service como bean
```

Documentación y API interactiva: `http://localhost:8080/swagger-ui.html`.

## Cómo correr la app

```bash
./gradlew bootRun
```

Ejemplo de flujo completo vía HTTP:

```bash
# Registrar un producto
curl -X POST http://localhost:8080/products -H "Content-Type: application/json" -d '{"sku":"SKU-001","name":"Café en grano 1kg"}'

# Recibir stock
curl -X POST http://localhost:8080/products/{id}/stock/receive -H "Content-Type: application/json" -d '{"quantity":50}'

# Reservar stock (usa el Domain Service: valida Product + StockItem)
curl -X POST http://localhost:8080/products/{id}/stock/reserve -H "Content-Type: application/json" -d '{"quantity":20}'

# Consultar nivel de stock
curl http://localhost:8080/products/{id}/stock

# Discontinuar (falla si aún queda stock)
curl -X POST http://localhost:8080/products/{id}/discontinue
```

## Referencias

- **Jeffrey Palermo** — [*The Onion Architecture*](https://jeffreypalermo.com/2008/07/the-onion-architecture-part-1/) (2008).
- Proyecto de referencia agregado por el usuario:
  [`coffee-shop-hexagonal-con-IA-main`](../../01-hexagonal-architecture/coffee-shop-hexagonal-con-IA-main)
  (basado en [arhohuttunen.com/hexagonal-architecture-spring-boot](https://www.arhohuttunen.com/hexagonal-architecture-spring-boot/)) —
  de ahí la convención de `domain/repository` para las interfaces de persistencia.
- Mismas referencias generales de Spring/DDD ya citadas en
  `01-hexagonal-architecture`/`03-clean-architecture`.

## Decisiones de diseño registradas

Ver `CLAUDE.md` para el detalle completo: tabla de contraste
Hexagonal/Clean/Onion, por qué `InventoryDomainService` no lleva `@Service`
de Spring, y por qué reservar/discontinuar necesitan ese Domain Service
(mismo problema multi-agregado que `TransferInteractor` en `03-`, resuelto
con una pieza de arquitectura distinta).
