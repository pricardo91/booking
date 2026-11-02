# Booking

[![CI](https://github.com/pricardo91/booking/actions/workflows/ci.yml/badge.svg)](https://github.com/pricardo91/booking/actions/workflows/ci.yml)

REST API for booking meeting rooms. It manages rooms (`Sala`), users (`Usuario`) and reservations (`Reserva`), with filtering, pagination, versioned database migrations and integration tests against a real PostgreSQL.

**Stack:** Java 17, Spring Boot 4, Spring Data JPA, PostgreSQL 17, Flyway, Testcontainers, Docker, GitHub Actions.

> The domain code (classes, endpoints, error messages) is in Portuguese on purpose; the project documentation is in English.

## Architecture

Each business module (`sala`, `usuario`, `reserva`) is split into four layers, and dependencies point inward:

```
api  ──►  application  ──►  domain
              ▲
infra  ───────┘  (implements the ports declared in application)
```

| Layer | Responsibility |
|---|---|
| `api` | REST controllers, request/response DTOs, Swagger contract |
| `application` | Use cases (services) and the repository **ports** (interfaces) |
| `domain` | Entities and business rules |
| `infra` | Port **adapters**: Spring Data JPA, `Specification` filters |

Cross-cutting code lives in `shared`: `GlobalExceptionHandler` turns business errors (`NegocioException` + `CodigoErro`) into RFC 9457 `ProblemDetail` responses.

## Running locally

### Prerequisites

- JDK 17
- Docker Desktop (running)

### 1. Configure the environment

Copy the example file and choose a password. `.env` is git-ignored and never committed.

```bash
cp .env.example .env
```

| Variable | Used by | Description |
|---|---|---|
| `DB_USER` | app, postgres | Database user (required) |
| `DB_PASSWORD` | app, postgres | Database password (required) |
| `DB_URL` | app | JDBC URL. Default: `jdbc:postgresql://localhost:5432/booking` |

### 2. Pick how to run

**Daily development (app in the IDE, database in Docker)**

```bash
docker compose up -d postgres
```

Then run `BookingApplication` from IntelliJ with `DB_USER` and `DB_PASSWORD` set in the run configuration (or loaded from `.env`). Flyway creates the schema on startup.

**Everything in Docker (production-like)**

```bash
docker compose up --build
```

The `app` container only starts after PostgreSQL reports healthy, and exposes its own health check.

Useful commands:

```bash
docker compose down        # stop, keeping the data
docker compose down -v     # stop and delete the database volume
```

The application listens on `http://localhost:8080`.

## API

Interactive documentation (Swagger UI): <http://localhost:8080/swagger-ui.html>
OpenAPI document: <http://localhost:8080/v1/api-docs>

| Resource | Endpoints |
|---|---|
| Salas | `POST /api/v1/salas`, `GET /api/v1/salas`, `GET /api/v1/salas/{id}`, `PUT /api/v1/salas/{id}`, `PATCH /api/v1/salas/{id}/inativar`, `PATCH /api/v1/salas/{id}/ativar` |
| Usuarios | `POST /api/v1/usuarios`, `GET /api/v1/usuarios`, `GET /api/v1/usuarios/{id}`, `PATCH /api/v1/usuarios/{id}`, `PATCH /api/v1/usuarios/{id}/inativar`, `PATCH /api/v1/usuarios/{id}/ativar` |
| Reservas | `POST /api/v1/reservas`, `GET /api/v1/reservas`, `GET /api/v1/reservas/{id}` |

`GET /api/v1/reservas` accepts the filters `status`, `idSala` and `idUsuario`, plus the pagination parameters `page` and `size` (1 to 100). Results come newest first.

New reservations are created as `PENDENTE`. The valid statuses are `PENDENTE`, `ATIVO`, `CANCELADO` and `FINALIZADO`.

### Health checks

| Endpoint | Meaning |
|---|---|
| `GET /actuator/health` | Overall status |
| `GET /actuator/health/liveness` | The process is alive |
| `GET /actuator/health/readiness` | The app is ready for traffic (includes the database) |

## Tests

```bash
./mvnw test
```

Docker must be running: the integration tests start a real PostgreSQL 17 with Testcontainers instead of an in-memory database, so they exercise the same SQL and Flyway migrations as production. The suite covers services (Mockito), the repository (filters, ordering, pagination and N+1 prevention) and the HTTP endpoints end to end.

Code style is enforced by Spotless:

```bash
./mvnw spotless:apply   # format
./mvnw spotless:check   # verify
```

## Continuous integration

Every pull request to `develop` runs [GitHub Actions](.github/workflows/ci.yml): `spotless:check` and the full test suite. The `develop` branch is protected and requires a pull request with a passing `build` check.

## Database migrations

The schema is versioned with Flyway in `src/main/resources/db/migration` and Hibernate only validates it (`ddl-auto: validate`). Never edit a migration that was already applied: add a new `V<n>__description.sql` instead.
