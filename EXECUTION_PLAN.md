# Execution Plan — EXIM Marketplace Backend

A phase-by-phase plan to build the backend defined in the TDD. Each phase lists the modules to implement, their entities and endpoints, the folders to add, and an acceptance check. Every module follows the same five-layer shape proven in the `identity` module:

```
<module>/domain      JPA entities + enums
<module>/repository  Spring Data JPA repositories
<module>/service     business logic (@Transactional)
<module>/controller  REST controllers (/api/v1/...)
<module>/dto         request/response objects
```

Build rule for each module: write a Flyway migration first (`Vn__<module>.sql`), then domain → repository → dto → service → controller, then tests.

---

## Phase 0 — Project setup  (DONE — this repository)
- Maven project, Spring Boot 4, profiles, Docker Postgres/Redis, Swagger.
- Common layer: `BaseEntity`, `ApiResponse`/`ApiError`, `GlobalExceptionHandler`.
- **Acceptance:** `mvn spring-boot:run` starts; Swagger UI loads.

## Phase 1 — Foundation & identity  (DONE — this repository)
- Modules: `common`, `config`, `security`, `identity`.
- JWT auth (register/login), RBAC roles, company-scoped users, password hashing.
- Migration: `V1__init_identity.sql` (companies, users).
- **Acceptance:** register → login → call `/api/v1/users/me` with the token.
- Next step to add here: refresh tokens, and Testcontainers-based integration tests (`@SpringBootTest` with a real Postgres).

## Phase 2 — Trust foundation (verification)
- Module: `verification`.
- Entities: `Verification` (type IEC/GST/RCMC/BANK, fileUrl, status).
- Endpoints: `POST/GET /api/v1/companies/{id}/verifications`; admin approval in Phase 6.
- Add: object-storage abstraction (`StorageService`) for uploaded KYC files (S3-compatible).
- Migration: `V2__verification.sql`.
- **Acceptance:** a company admin can upload a KYC document and see its status.

## Phase 3 — Discovery (catalog + sourcing)
- Modules: `catalog`, `sourcing`.
- Entities: `Product`, `HsCode`, `Rfq`.
- Endpoints: `/api/v1/products`, `/api/v1/hs-codes`, `/api/v1/rfqs`, `/api/v1/rfqs/{id}/matches`.
- Add: OpenSearch integration for product/RFQ search and matching; index on create/update via the queue.
- Migrations: `V3__catalog.sql`, `V4__sourcing.sql`.
- **Acceptance:** a seller lists a product; a buyer posts an RFQ and gets matches.

## Phase 4 — The deal (quotation + messaging + orders + documents)
- Modules: `quotation`, `messaging`, `orders`, `documents`.
- Entities: `Quote`, `ProformaInvoice`, `Conversation`, `Message`, `Offer`, `Order`, `OrderItem`, `TradeDocument`.
- Endpoints: quote submit/accept/reject/counter; conversations + messages; WebSocket `/ws`; orders + status; document generation.
- Add: Redis (cache + WebSocket pub/sub), RabbitMQ (async document generation), PDF templating service. Optimistic locking + idempotency keys on order creation.
- Migrations: `V5__quotation.sql` … `V8__documents.sql`.
- **Acceptance:** accept a quote → order is created → generate a proforma/commercial invoice; buyer and seller chat in real time.

## Phase 5 — Execution (logistics + payments)
- Modules: `logistics`, `payments`.
- Entities: `Shipment`, `LogisticsPartner`, `Transaction` (escrow), `PaymentTerm`, `LetterOfCredit`.
- Endpoints: shipment create/track; payment initiate; escrow release.
- Add: licensed payment/escrow partner integration with signature-verified webhooks; serializable isolation for fund-affecting operations.
- Migrations: `V9__logistics.sql`, `V10__payments.sql`.
- **Acceptance:** an order can be shipped, tracked, and its escrow milestone released through the partner sandbox.

## Phase 6 — Revenue & operations (billing + trust + notification + admin)
- Modules: `billing`, `trust`, `notification`, `admin`.
- Entities: `Plan`, `Subscription`, `PlatformPayment`, `Rating`, `Dispute`, `Notification`.
- Endpoints: plans + subscription management (your revenue), ratings/disputes, notifications, admin KYC approval + sanctions screening + metrics.
- Add: billing provider integration, FCM push for the mobile app, observability (Micrometer/Prometheus, structured logs, tracing), rate limiting via Redis.
- Migrations: `V11__billing.sql` … `V14__notification.sql`.
- **Acceptance:** a company subscribes to a paid plan; plan limits are enforced; admin can approve KYC; ratings post after order completion.

---

## Cross-cutting work (in parallel, hardened by Phase 6)
- Refresh-token rotation + revocation list (Redis).
- Per-resource company-ownership authorization checks on every trade endpoint.
- Testcontainers integration tests per module; aim >70% service-layer coverage.
- CI/CD: build → test → image → staging → production; Flyway as a gated step.
- Load testing before launch; multi-AZ managed Postgres; daily backups + tested restore.

## Suggested order of work within any module
1. `Vn__<module>.sql` migration (tables, indexes, constraints).
2. `domain` entities (extend `BaseEntity`, use `@Version`, enums as STRING).
3. `repository` interfaces (derive queries; add `@Query` where needed).
4. `dto` request/response + Bean Validation annotations.
5. `service` with `@Transactional`; never expose entities directly.
6. `controller` returning `ApiResponse<T>`; secure with role + ownership checks.
7. tests (unit on service, integration on controller with Testcontainers).
