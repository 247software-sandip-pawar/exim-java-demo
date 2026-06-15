# EXIM Marketplace Backend

Spring Boot backend for an export-import B2B marketplace. This repository is the **Phase-1 foundation**: a runnable application with authentication, security, and the fully implemented `identity` module, plus the package structure for every other module ready to be filled in (see `EXECUTION_PLAN.md`).

### Architecture: one app, one database per module
This is a single deployable Spring Boot app, but **each module owns its own PostgreSQL database** (the `identity` module uses `exim_identity`). Per-module datasources, JPA contexts, transaction managers, and Flyway migrations are wired in code under `com.eximplatform.config.datasource` — `IdentityDataSourceConfig` is the template to copy for each new module. This keeps strict data ownership between modules (the seam needed to later extract one into its own service) while staying simple to run as a single process today.

## Tech stack
- Java 17, Spring Boot 4.0
- Spring Web, Spring Data JPA, Spring Security (JWT), Bean Validation, Actuator
- PostgreSQL + Flyway migrations
- springdoc-openapi (Swagger UI)
- Maven

## Prerequisites
- JDK 17+
- Maven 3.9+ (or use the bundled `mvnw` if you generate one)
- Docker (for local PostgreSQL)

## Run it locally

1. Create the per-module database(s). Phase 1 needs `exim_identity`.
   - **Using your own PostgreSQL (pgAdmin):** open a Query Tool on the `postgres` database and run `src/main/resources/db/bootstrap/create-databases.sql` (it runs `CREATE DATABASE exim_identity;`).
   - **Using Docker instead:** `docker compose up -d` — the bootstrap script is mounted into the container and the databases are created automatically on first start. (Also starts Redis, used from Phase 4.)
2. Point the app at your database. The `dev` profile defaults to `localhost:5432`, user `postgres`, password `postgres` — edit `src/main/resources/application-dev.yml` if your local password differs.
3. Run the app (uses the `dev` profile by default):
   ```bash
   mvn spring-boot:run
   ```
4. Open the API docs: http://localhost:8080/swagger-ui.html

Each module's Flyway migrations run against that module's own database on startup.

## Try the API
```bash
# Register a company + admin user
curl -s -X POST http://localhost:8080/api/v1/auth/register \
  -H "Content-Type: application/json" \
  -d '{"companyName":"Acme Exports","companyType":"EXPORTER","country":"India","adminName":"Asha","email":"asha@acme.com","password":"password123"}'

# Log in
curl -s -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"asha@acme.com","password":"password123"}'

# Call a protected endpoint (paste the accessToken from above)
curl -s http://localhost:8080/api/v1/users/me \
  -H "Authorization: Bearer <accessToken>"
```

## Switching the database (local -> cloud)
The app talks to plain PostgreSQL, so moving to any managed Postgres (Neon, AWS RDS, Cloud SQL, ...) is a config change, not a code change. Set the `prod` profile and the per-module env vars (one set per module):
```bash
export SPRING_PROFILES_ACTIVE=prod
export JWT_SECRET=<a-long-random-secret-of-at-least-32-bytes>

# identity module
export IDENTITY_DB_URL=jdbc:postgresql://<host>:5432/exim_identity
export IDENTITY_DB_USER=<user>
export IDENTITY_DB_PASSWORD=<password>
```
Each module's Flyway migrations recreate its schema on the target database; no rewrite needed.

## Project structure
```
src/main/java/com/eximplatform
  EximBackendApplication.java     app entry point
  common/      base entity, API response envelope, exception handling
  config/      OpenAPI/Swagger config
  config/datasource/  per-module datasource + JPA + Flyway wiring (IdentityDataSourceConfig = template)
  security/    JWT service, auth filter, security config
  identity/    FULLY IMPLEMENTED: company + user + auth (the template), backed by the exim_identity DB
  verification/ catalog/ sourcing/ quotation/ messaging/ orders/
  documents/ logistics/ payments/ trust/ billing/ notification/ admin/
               (folder structure + package-info; implement per EXECUTION_PLAN.md)
src/main/resources
  application.yml / application-dev.yml / application-prod.yml
  db/bootstrap/create-databases.sql   one CREATE DATABASE per module
  db/migration/identity/V1__init_identity.sql   identity module schema (own DB)
```

Each module follows the same five-layer shape as `identity`: `domain` -> `repository` -> `service` -> `controller` -> `dto`.

## Build a deployable jar
```bash
mvn clean package
java -jar target/exim-marketplace-backend-0.0.1-SNAPSHOT.jar
```

## Notes
- `springdoc-openapi` 3.0.x targets Spring Boot 4; if you change the Boot version, align springdoc per its compatibility matrix.
- Payments/escrow and KYC are regulated; integrate a licensed partner rather than custodying funds yourself (see the TDD, compliance section).
