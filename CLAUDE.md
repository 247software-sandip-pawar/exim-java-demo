# CLAUDE.md

Guidance for working in this repository. Read this first.

## What this is
EXIM Marketplace: an export-import B2B platform built as **module-wise microservices** in a Maven
multi-module monorepo. Each business module is an independently deployable Spring Boot service that
**owns its own PostgreSQL database**; services communicate over REST and sit behind one API gateway.
Built phase-by-phase per `EXECUTION_PLAN.md`.

## Topology
```
client → gateway(:8080) → identity(:8081) | verification(:8082) | catalog(:8083) | sourcing(:8084)
                          exim_identity      exim_verification     exim_catalog      exim_sourcing
```
- identity issues JWTs (register/login); **every service validates JWTs independently** with the
  same `JWT_SECRET`. No shared session, no shared database.
- Cross-module data is fetched over REST, never by touching another service's DB. Clients live in
  `libs/common/.../client`: `IdentityClient.companyExists(id)`, `CatalogClient.findProductsByHsCode(code)`.
  Both forward the caller's `Authorization` header.

## Repository layout
```
pom.xml                      parent / aggregator (modules list + dependencyManagement; spring-cloud BOM)
libs/
  common/                    ApiResponse/ApiError/PageResponse, BaseEntity, GlobalExceptionHandler,
                             OpenApiConfig, client/{IdentityClient,CatalogClient,ProductMatch}
  security/                  JwtService, JwtAuthenticationFilter, SecurityConfig (shared, property-driven)
services/<name>-service/     a Spring Boot app: domain → repository → service → controller → dto,
                             plus config/datasource/<Name>DataSourceConfig + resources/db/migration/<name>
gateway/                     Spring Cloud Gateway (servlet/webmvc); routes only, no auth
infra/db/create-databases.sql  one CREATE DATABASE per service
```

## Conventions (follow these — several are easy to get wrong)
- **Package root is always `com.eximplatform`.** Each service's `@SpringBootApplication` sets
  `scanBasePackages = "com.eximplatform"` so the shared `common`/`security` beans on the classpath
  get component-scanned. Keep package names identical across modules (no per-service prefixes).
- **`@Transactional` MUST name the service's transaction manager**, e.g.
  `@Transactional(transactionManager = "catalogTransactionManager")`. There is no `@Primary` tx
  manager in standalone services; an unqualified `@Transactional` will resolve wrong/fail.
- **Schema is owned by Flyway, validated by Hibernate.** Each `*DataSourceConfig` runs Flyway
  (`classpath:db/migration/<module>`) and sets `hibernate.hbm2ddl.auto=validate`. Write the
  migration first; entity columns must match it (camelCase → snake_case naming strategy).
- **Entities** extend `common.domain.BaseEntity` (UUID id, `@Version`, `created_at`/`updated_at`).
  Enums are persisted `@Enumerated(EnumType.STRING)`.
- **Never return JPA entities from services.** Map to DTOs *inside* the transaction (`open-in-view`
  is false). DTOs use a static `from(entity)` factory.
- **Responses** are always wrapped: `ApiResponse<T>` (`{success,data,error,timestamp}`); list
  endpoints wrap `PageResponse<T>`. Controllers return `ApiResponse.ok(...)`.
- **Errors** are mapped centrally in `common.GlobalExceptionHandler`: `ApiException`/`BusinessException`
  (422) / `NotFoundException` (404), `MethodArgumentNotValidException` (400), `AccessDeniedException`
  (403), `HttpRequestMethodNotSupportedException` (405). Throw these; don't hand-craft error bodies.
- **Security**: the shared `SecurityConfig` is stateless + JWT-only, `@EnableMethodSecurity` on.
  Public paths come from `app.security.public-paths` (identity sets `/api/v1/auth/**`); Swagger +
  `/actuator/health` are always public. Authorize endpoints with `@PreAuthorize("hasRole('PLATFORM_ADMIN')")`
  etc. Roles: `COMPANY_ADMIN, COMPANY_MEMBER, PLATFORM_ADMIN, SUPPORT` (JWT authority = `ROLE_<role>`).
  Only identity-service has `PasswordEncoder`/`AuthenticationManager`/`UserDetailsService`.

## Build / run / test
```bash
mvn clean install                                   # whole reactor
mvn -pl services/catalog-service test               # one module
mvn -pl services/identity-service spring-boot:run   # run one service (dev profile by default)
mvn -pl gateway spring-boot:run                     # run the gateway (needs the others up to proxy)
```
- Tests are **Mockito service-layer unit tests, no DB** (so `mvn install` stays green without Postgres).
  The gateway has a context-load `@SpringBootTest`. There are no Testcontainers integration tests yet.
- To run the stack: `docker compose up -d` (Postgres with all DBs + Redis), then `spring-boot:run` each
  service. All services must share the same `JWT_SECRET` (defaulted in dev).

## Adding a new service (e.g. `orders`)
1. `infra/db/create-databases.sql`: add `CREATE DATABASE exim_orders;`
2. New module under `services/orders-service` + add it to the parent `pom.xml` `<modules>`.
3. Copy a `*DataSourceConfig` (e.g. catalog's), rename beans to `orders*`, point `@EnableJpaRepositories`
   at `com.eximplatform.orders.repository` and Flyway at `db/migration/orders`.
4. `OrdersServiceApplication` with `@SpringBootApplication(scanBasePackages = "com.eximplatform")`.
5. Migration `V1__init_orders.sql` first, then domain → repository → dto → service
   (`@Transactional(transactionManager="ordersTransactionManager")`) → controller (`@PreAuthorize`).
6. `application.yml` (own port, `app.datasource.orders.*`, `JWT_SECRET`, any `app.clients.*` URLs) +
   `application-dev.yml` / `application-prod.yml`.
7. Add a route to `gateway/src/main/resources/application.yml` (order specific paths before general ones).
8. Cross-module reads: add/extend a REST client in `libs/common` rather than depending on another
   service's code or database.

## Gotchas
- **Port 8080**: the gateway uses it. A stray older monolith process may hold it — free it first.
- **Gateway route order matters** (first match wins). `/api/v1/companies/*/verifications` must precede
  `/api/v1/companies/**`, or identity would swallow the verification sub-path.
- **First gateway build needs network** to fetch Spring Cloud (`spring-cloud 2025.1`, gateway `5.0.0`);
  other modules build from the cached Spring Boot 4.0 deps.
- **JWT lifetime is 15 min**, no refresh token yet. Expired/invalid tokens are logged at DEBUG in
  `JwtAuthenticationFilter` and rejected with 403 by the protected routes.

## Status & deferred work
- Implemented: Phase 1 identity, Phase 2 verification, Phase 3 catalog + sourcing.
- Deferred (noted in code): refresh tokens; per-resource company-ownership checks (a COMPANY_ADMIN
  currently can act on any company id); real KYC file upload (today a `fileUrl` string, no StorageService);
  async/event messaging; Testcontainers integration tests; per-service container images.
- Next: Phase 4 (quotation, messaging, orders, documents) — see `EXECUTION_PLAN.md`.
