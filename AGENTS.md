# Auth Service

Authentication & Authorization microservice for the Apized platform.

## Stack

- **Java 21** + **Micronaut** framework
- **PostgreSQL** (via Flyway migrations)
- **RabbitMQ** for async events
- **JWT** (auth0 java-jwt) for token management
- **Gradle** multi-project build (`:server`, `:client`)
- **GraalVM** native image support
- Tests in **Groovy + Cucumber (BDD)** with TestContainers

## Project Structure

```
server/   - Main auth server (Micronaut app)
client/   - Java client library for other Apized services
```

## Build & Test

```bash
./gradlew clean build          # Build everything
./gradlew clean test           # Run integration tests
./gradlew :server:run          # Run server locally
./gradlew :client:publish      # Publish client JAR
```

## Key Env Vars

| Variable | Default | Purpose |
|---|---|---|
| `PORT` | `80` | Server port |
| `DB_HOST/PORT/NAME/USER/PASS` | — | PostgreSQL connection |
| `AUTH_TOKEN_SECRET` | `boatymcboatface` | JWT signing secret |
| `AUTH_TOKEN_DURATION` | `3600` | Token TTL (seconds) |
| `RABBITMQ_URI/USERNAME/PASSWORD` | — | RabbitMQ connection |
| `OTEL_ENABLED/OTEL_ENDPOINT` | — | OpenTelemetry tracing |

## Domain Model

- **User** — accounts with email verification, BCrypt password hashing
- **Role** — RBAC roles with permissions, assigned to users
- **Token** — JWT generation, validation, renewal, login/logout
- **OAuth** — provider configurations for social login

## Source Layout

```
server/src/main/java/org/apized/auth/
  api/user/    - User CRUD, password reset, verification
  api/role/    - Role & permission management
  api/token/   - Login, token generation/renewal
  api/oauth/   - OAuth provider integration
  oauth/       - OAuth client implementations
  security/    - Auth/encryption utilities
```

## Database Migrations

Flyway migrations live in `server/src/main/resources/db/migration/`.

## Testing Conventions

- Integration tests: `server/src/test/groovy/`
- BDD feature files: `server/src/test/resources/features/`
- Uses TestContainers for PostgreSQL — no mocking of the database

## Apized Framework

This project uses the Apized framework (`apizedVersion=2.0.2`). Use the `apized` skill when working with framework-specific patterns (behaviors, repository extensions, permissions, federation).

## Versions

- Project version: `gradle.properties` → `version`
- Apized version: `gradle.properties` → `apizedVersion`
- Release managed by Gradle Release Plugin
