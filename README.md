# EXIM Marketplace Platform

Backend for an export-import B2B marketplace, built as **module-wise microservices** in a Maven
multi-module monorepo. Each business module is an independently deployable Spring Boot service that
owns its own PostgreSQL database; services talk to each other over REST and sit behind a single API
gateway.

## Architecture

```
                       ┌─────────────────────────┐
   client  ───────────▶│  gateway  (:8080)       │   Spring Cloud Gateway (webmvc)
                       └───────────┬─────────────┘   routes by path; services validate JWT
            ┌──────────────────────┼───────────────────────┬─────────────────────┐
            ▼                      ▼                       ▼                     ▼
   identity-service       verification-service      catalog-service       sourcing-service
       (:8081)                 (:8082)                  (:8083)               (:8084)
   exim_identity            exim_verification         exim_catalog          exim_sourcing
   auth/users/companies     KYC documents             products/HS codes     RFQs + matching
                               │                            ▲                     │
                               └── REST: companyExists ─────┘   REST: products ───┘
                                       (IdentityClient)          by HS code (CatalogClient)
```

- **Shared libraries** (`libs/`): `common` (API envelopes, `BaseEntity`, global exception handling,
  OpenAPI config, the REST `IdentityClient`/`CatalogClient`) and `security` (JWT validation, auth
  filter, a property-driven stateless `SecurityConfig`).
- **Stateless auth**: identity-service issues JWTs; every service validates them independently with
  the shared secret. No shared session, no shared database.
- **No cross-service DB access**: a service that needs another module's data calls it over REST and
  forwards the caller's JWT (e.g. verification/catalog/sourcing → identity to check a company).

## Tech stack
- Java 17, Spring Boot 4.0, Spring Cloud 2025.1 (Gateway)
- Spring Web, Spring Data JPA, Spring Security (JWT), Bean Validation, Actuator
- PostgreSQL + Flyway (per-service migrations), springdoc-openapi (Swagger UI per service), Maven

## Repository layout
```
pom.xml                       parent / aggregator (modules + dependency management)
libs/
  common/                     API envelopes, BaseEntity, exception handler, IdentityClient/CatalogClient
  security/                   JwtService, JwtAuthenticationFilter, shared SecurityConfig
services/
  identity-service/    :8081  auth, users, companies          (owns exim_identity)
  verification-service/:8082  company KYC                      (owns exim_verification)
  catalog-service/     :8083  products, HS codes               (owns exim_catalog)
  sourcing-service/    :8084  RFQs + matching                  (owns exim_sourcing)
gateway/               :8080  Spring Cloud Gateway (entry point)
infra/db/                     create-databases.sql (one CREATE DATABASE per service)
```

Each service follows the same five-layer shape: `domain → repository → service → controller → dto`,
plus a per-service `config/datasource` class wiring its own database + Flyway.

## Run it locally

1. **Databases.** Create one database per service (Phase 1–3 need identity, verification, catalog,
   sourcing):
   - Docker: `docker compose up -d` (the bootstrap script creates them on first start; also starts Redis).
   - Or your own PostgreSQL: run `infra/db/create-databases.sql` against the `postgres` database.
2. **Build everything:** `mvn clean install`
3. **Run each service** (separate terminals; `dev` profile by default):
   ```bash
   mvn -pl services/identity-service     spring-boot:run
   mvn -pl services/verification-service spring-boot:run
   mvn -pl services/catalog-service      spring-boot:run
   mvn -pl services/sourcing-service     spring-boot:run
   mvn -pl gateway                       spring-boot:run
   ```
4. Everything is reachable through the gateway at **http://localhost:8080**. Each service also serves
   its own Swagger UI (e.g. http://localhost:8081/swagger-ui.html).

> All services must share the same `JWT_SECRET` (defaulted for dev). In dev they default to
> `localhost:5432`, user/password `postgres`/`postgres` — edit each service's `application-dev.yml`
> if your local Postgres differs.

## Try the API (through the gateway)
```bash
# 1. Register a company + admin (identity)
curl -s -X POST http://localhost:8080/api/v1/auth/register \
  -H "Content-Type: application/json" \
  -d '{"companyName":"Acme Exports","companyType":"EXPORTER","country":"India","adminName":"Asha","email":"asha@acme.com","password":"password123"}'

# 2. List HS codes (catalog, seeded) — needs the Bearer token from step 1
curl -s http://localhost:8080/api/v1/hs-codes -H "Authorization: Bearer <token>"

# 3. List a product (catalog), then post an RFQ (sourcing) and get matches
curl -s "http://localhost:8080/api/v1/rfqs/<rfqId>/matches" -H "Authorization: Bearer <token>"
```

## Production
Set `SPRING_PROFILES_ACTIVE=prod`, a shared `JWT_SECRET`, each service's `*_DB_*` env vars, and the
inter-service base URLs (`IDENTITY_BASE_URL`, `CATALOG_BASE_URL`) / gateway route URIs. Each service's
Flyway migrations recreate its schema on its own target database.

## Build a deployable jar (per service)
```bash
mvn -pl services/identity-service -am clean package
java -jar services/identity-service/target/identity-service-0.0.1-SNAPSHOT.jar
```

## Status
- **Phase 1 (identity)**, **Phase 2 (verification)**, **Phase 3 (catalog + sourcing)** implemented.
- See `EXECUTION_PLAN.md` for the remaining phases.
- Deferred/cross-cutting: refresh tokens, per-resource company-ownership checks, real file upload for
  KYC (currently a `fileUrl`), async events, and per-service container images.
