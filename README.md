# EXIM Marketplace Backend

Spring Boot backend for an export-import B2B marketplace. This repository is the **Phase-1 foundation**: a runnable application with authentication, security, and the fully implemented `identity` module, plus the package structure for every other module ready to be filled in (see `EXECUTION_PLAN.md`).

## Tech stack
- Java 21, Spring Boot 4.0
- Spring Web, Spring Data JPA, Spring Security (JWT), Bean Validation, Actuator
- PostgreSQL + Flyway migrations
- springdoc-openapi (Swagger UI)
- Maven

## Prerequisites
- JDK 21+
- Maven 3.9+ (or use the bundled `mvnw` if you generate one)
- Docker (for local PostgreSQL)

## Run it locally

1. Start PostgreSQL (and Redis, used from Phase 4):
   ```bash
   docker compose up -d
   ```
2. Run the app (uses the `dev` profile by default):
   ```bash
   mvn spring-boot:run
   ```
3. Open the API docs: http://localhost:8080/swagger-ui.html

Flyway creates the schema automatically on first start.

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
The app talks to plain PostgreSQL, so moving to any managed Postgres (Neon, AWS RDS, Cloud SQL, ...) is a config change, not a code change. Set the `prod` profile and these env vars:
```bash
export SPRING_PROFILES_ACTIVE=prod
export DB_URL=jdbc:postgresql://<host>:5432/<db>
export DB_USER=<user>
export DB_PASSWORD=<password>
export JWT_SECRET=<a-long-random-secret-of-at-least-32-bytes>
```
Your Flyway migrations recreate the schema on the new database; no rewrite needed.

## Project structure
```
src/main/java/com/eximplatform
  EximBackendApplication.java     app entry point
  common/      base entity, API response envelope, exception handling
  config/      OpenAPI/Swagger config
  security/    JWT service, auth filter, security config
  identity/    FULLY IMPLEMENTED: company + user + auth (the template)
  verification/ catalog/ sourcing/ quotation/ messaging/ orders/
  documents/ logistics/ payments/ trust/ billing/ notification/ admin/
               (folder structure + package-info; implement per EXECUTION_PLAN.md)
src/main/resources
  application.yml / application-dev.yml / application-prod.yml
  db/migration/V1__init_identity.sql
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
