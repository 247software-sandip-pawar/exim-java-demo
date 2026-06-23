# CLAUDE.md

Guidance for working in this repository. Read this first.

## What this is
EXIM Marketplace: an export-import B2B platform built as **module-wise microservices** in a Maven
multi-module monorepo. Each business module is an independently deployable Spring Boot service that
**owns its own MongoDB database**; services communicate over REST and sit behind one API gateway.
Built phase-by-phase per `EXECUTION_PLAN.md`. Persistence is **Spring Data MongoDB**; the target
store is **MongoDB Atlas** (one cluster, one database per service), see `MONGODB_SETUP.md`.

## Topology
```
client → gateway(:8080) → identity(:8081) | verification(:8082) | catalog(:8083) | sourcing(:8084)
                          quotation(:8085) | messaging(:8086)    | orders(:8087)  | documents(:8088)
```
  exim_identity exim_verification exim_catalog exim_sourcing exim_quotation exim_messaging
  exim_orders exim_documents  (one MongoDB database per service)
- identity issues JWTs (register/login); **every service validates JWTs independently** with the
  same `JWT_SECRET`. No shared session, no shared database. All services share one `MONGODB_URI`
  (the cluster) but each writes to its own database via `spring.data.mongodb.database`.
- Cross-module data is fetched over REST, never by touching another service's DB. Clients live in
  `libs/common/.../client`: `IdentityClient.companyExists(id)`, `CatalogClient.findProductsByHsCode(code)`,
  `QuotationClient.getQuote(id)` (orders builds an order from an accepted quote),
  `OrdersClient.getOrder(id)` (documents generates a trade document for an order).
  All forward the caller's `Authorization` header.
- The deal flow (Phase 4): a seller submits a quote → buyer accepts → orders creates exactly one
  order per accepted quote (idempotent on `quoteId`) → documents generates trade documents for the
  order. Buyer/seller chat in `messaging` (conversations + messages, optional in-chat `Offer`).

## Repository layout
```
pom.xml                      parent / aggregator (modules list + dependencyManagement; spring-cloud BOM)
libs/
  common/                    ApiResponse/ApiError/PageResponse, BaseEntity, GlobalExceptionHandler,
                             OpenApiConfig, client/{IdentityClient,CatalogClient,QuotationClient,
                             OrdersClient,ProductMatch,QuoteView,OrderView}
  security/                  JwtService, JwtAuthenticationFilter, SecurityConfig (shared, property-driven)
services/<name>-service/     a Spring Boot app: domain → repository → service → controller → dto,
                             plus config/<Name>MongoConfig (repositories + auditing + tx manager)
gateway/                     Spring Cloud Gateway (servlet/webmvc); routes only, no auth
docker-compose.yml           optional local MongoDB (single-node replica set) + Redis
```
MongoDB auto-creates databases/collections on first write, so there is no `create-databases.sql`
and no schema migration step. Reference/seed data is inserted on startup by a `CommandLineRunner`
(e.g. catalog's `HsCodeSeeder`).

## Conventions (follow these — several are easy to get wrong)
- **Package root is always `com.eximplatform`.** Each service's `@SpringBootApplication` sets
  `scanBasePackages = "com.eximplatform"` so the shared `common`/`security` beans on the classpath
  get component-scanned. Keep package names identical across modules (no per-service prefixes).
- **`@Transactional` names the service's transaction manager** (a `MongoTransactionManager`), e.g.
  `@Transactional(transactionManager = "catalogTransactionManager")`, declared in each
  `*MongoConfig`. identity-service uses a plain `@Transactional` (single tx manager bean → resolves
  unambiguously). Mongo multi-doc transactions require a **replica set** (Atlas is one; the local
  docker-compose Mongo runs as a single-node replica set for this reason).
- **No schema, no migrations.** Collections are schemaless; `spring.data.mongodb.auto-index-creation`
  builds indexes from `@Indexed`/`@Indexed(unique=true)` annotations on startup. Seed reference data
  with a `CommandLineRunner` (insert-if-empty), not a migration.
- **No dirty-checking (IMPORTANT).** MongoDB has no JPA-style flush-on-commit. An `update()` that
  mutates a loaded document MUST call `repository.save(...)` explicitly, or the change is lost.
- **Entities** are `@Document(collection="...")` and extend `common.domain.BaseEntity`
  (app-assigned `UUID id`, **nullable `Long` `@Version`** — a primitive `long` defaulting to 0 is
  misread as "new" on every save — and `@CreatedDate`/`@LastModifiedDate`, enabled by
  `@EnableMongoAuditing`). Enums persist as their `String` name by default. Cross-collection links
  inside a service use `@DBRef` (e.g. `User.company`); links across services are a bare `UUID`.
- **Never return documents from services.** Map to DTOs inside the service method. DTOs use a static
  `from(entity)` factory.
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
- Tests are **Mockito service-layer unit tests, no DB** (so `mvn install` stays green without MongoDB).
  The gateway has a context-load `@SpringBootTest`. There are no Testcontainers integration tests yet.
- To run the stack against **Atlas**: `export MONGODB_URI='mongodb+srv://<user>:<pass>@<cluster>/'`
  (one secret, every service writes to its own database), then `spring-boot:run` each service.
  To run **offline**: `docker compose up -d` (local single-node-replica-set Mongo + Redis) — the dev
  default `MONGODB_URI` already points at it. All services must share the same `JWT_SECRET` and
  `MONGODB_URI`. See `MONGODB_SETUP.md` for the Atlas account walkthrough.

## Adding a new service (e.g. `orders`)
1. New module under `services/orders-service` + add it to the parent `pom.xml` `<modules>`
   (depend on `common` + `security`, and `spring-boot-starter-data-mongodb`).
2. Copy a `*MongoConfig` (e.g. catalog's), rename the tx-manager bean to `ordersTransactionManager`,
   point `@EnableMongoRepositories` at `com.eximplatform.orders.repository`.
3. `OrdersServiceApplication` with `@SpringBootApplication(scanBasePackages = "com.eximplatform")`.
4. domain (`@Document`, extend `BaseEntity`, `@Indexed` where needed) → repository (`MongoRepository`)
   → dto → service (`@Transactional(transactionManager="ordersTransactionManager")`, **explicit
   `save()` on updates**) → controller (`@PreAuthorize`). Seed reference data with a `CommandLineRunner`.
5. `application.yml` (own port, `spring.data.mongodb.uri`=`${MONGODB_URI:...}` + `database: exim_orders`,
   `JWT_SECRET`, any `app.clients.*` URLs) + `application-dev.yml` / `application-prod.yml`.
6. Add a route to `gateway/src/main/resources/application.yml` (order specific paths before general ones).
7. Cross-module reads: add/extend a REST client in `libs/common` rather than depending on another
   service's code or database.

## Gotchas
- **Port 8080**: the gateway uses it. A stray older monolith process may hold it — free it first.
- **Gateway route order matters** (first match wins). `/api/v1/companies/*/verifications` must precede
  `/api/v1/companies/**`, or identity would swallow the verification sub-path.
- **First gateway build needs network** to fetch Spring Cloud (`spring-cloud 2025.1`, gateway `5.0.0`);
  other modules build from the cached Spring Boot 4.0 deps (incl. the MongoDB driver/starter).
- **Mongo transactions need a replica set.** A standalone `mongod` throws on `@Transactional` writes;
  use Atlas (a replica set) or the single-node-replica-set `docker compose` Mongo. `directConnection=true`
  in the dev URI lets the client talk to that single node without SRV discovery.
- **JWT lifetime is 15 min**, no refresh token yet. Expired/invalid tokens are logged at DEBUG in
  `JwtAuthenticationFilter` and rejected with 403 by the protected routes.

## Status & deferred work
- Implemented: Phase 1 identity, Phase 2 verification, Phase 3 catalog + sourcing,
  Phase 4 quotation + messaging + orders + documents.
- Deferred (noted in code): refresh tokens; per-resource company-ownership checks (a COMPANY_ADMIN
  currently can act on any company id); real KYC file upload (today a `fileUrl` string, no StorageService);
  Testcontainers integration tests; per-service container images. **Phase 4 infra is deliberately
  deferred** (the modules are MongoDB + REST only): no RabbitMQ async document generation, no Redis,
  no WebSocket `/ws` for real-time chat (messaging is plain REST), and documents records a `fileUrl`
  placeholder rather than rendering a real PDF.
- Next: Phase 5 (logistics + payments) — see `EXECUTION_PLAN.md`. Note that `EXECUTION_PLAN.md`
  still describes the pre-migration JPA/Flyway/Postgres stack; the MongoDB conventions in this file
  are authoritative.
