# Hirkani Exim — UI Build & Backend-Wiring Plan

Goal: build the authenticated app UI **module by module**, wiring each screen to the real
backend through the gateway (`:8080`), and **verify each module end-to-end** before moving on.

Phases mirror the backend's own Phase 1–6 ordering, because the UI dependencies follow the
deal flow (you can't quote without an RFQ, can't order without an accepted quote, etc.).

---

## Conventions used below

- **Endpoints** are gateway paths (`/api/v1/...`). All responses are
  `ApiResponse<T>` → `{ success, data, error, timestamp }`; lists wrap
  `PageResponse<T>` → `{ items, page, size, totalElements, totalPages }`.
- **Roles:** `COMPANY_ADMIN`, `COMPANY_MEMBER`, `PLATFORM_ADMIN`, `SUPPORT`.
- **"Definition of working"** = the concrete click-path that proves the module is wired
  correctly against the running backend. This is the acceptance check per phase.
- Each phase reuses the design system in `DESIGN_GUIDELINES.md` (navy/gold tokens,
  shadcn-pattern components).

---

## Phase 0 — Frontend foundation (do this first, no business screens)

The plumbing every later phase depends on. ~1–2 days.

**Add to the stack**
- `@tanstack/react-query` — server-state cache, loading/error states, refetch.
- `axios` — one HTTP client with interceptors.
- `react-hook-form` + `zod` + `@hookform/resolvers` — typed forms & validation.
- `sonner` — toast notifications (errors/success).

**Build**
1. **Vite dev proxy** — `/api → http://localhost:8080` in `vite.config.js` (kills CORS in dev).
   For prod, serve behind same origin or enable CORS on the gateway.
2. **API client** (`src/lib/api.js`) — axios instance, base `/api/v1`, request interceptor
   attaches `Authorization: Bearer <token>`, response interceptor unwraps `ApiResponse.data`
   and maps `error` → thrown `ApiError`. On `401/403` (expired 15-min JWT) → clear token,
   redirect to login, toast "session expired".
3. **Auth context** (`src/auth/`) — `login()`, `register()`, `logout()`, current user from
   `/users/me`, token in memory + `localStorage`, `useAuth()` hook. (No refresh token yet —
   known backend gap; just re-login on expiry.)
4. **Routing** — `ProtectedRoute` (must be authed) and `RoleRoute` (must have role) wrappers.
   Split routes: public marketing (existing) vs. `/app/*` authenticated shell.
5. **App shell** (`src/components/app/`) — sidebar nav (sections appear per role), topbar with
   company name + user menu + notification bell + logout. Reuse navy/gold tokens.
6. **Shared app UI** — `DataTable`, `FormField`, `Dialog/Modal`, `StatusBadge`,
   `EmptyState`, `LoadingState`, `ErrorState`, `Pagination` (zero-indexed to match backend),
   `PageHeader`. Build once, reuse everywhere.
7. **Query hooks pattern** — `useApiQuery`/`useApiMutation` wrappers around React Query +
   the api client, so every module follows the same shape.

**Definition of working:** start one backend service + gateway; hitting a protected route
unauthenticated redirects to login; a hand-issued token loads `/users/me` in the topbar.

---

## Phase 1 — Identity & Auth  *(identity-service :8081)* — the gate

Validates the whole auth chain. Everything else needs a token.

| Screen | Endpoints | Roles |
|--------|-----------|-------|
| Register (company + admin) | `POST /auth/register` | public |
| Login | `POST /auth/login` (returns `accessToken`, `user`) | public |
| Profile / "My account" | `GET /users/me` | authenticated |
| Company profile view | `GET /companies/{id}`, `GET /companies` | authenticated |
| **Admin:** User management (list/create/edit/delete) | `GET/POST/PUT/DELETE /users`, `GET /users/{id}` | PLATFORM_ADMIN, SUPPORT (read) |
| **Admin:** Company management | `POST/PUT/DELETE /companies` | PLATFORM_ADMIN |

**Definition of working:** register a company → auto-login or login → token stored →
topbar shows the real user/company from `/users/me` → refresh keeps you logged in →
logout clears it. Create a 2nd company (seller) and a PLATFORM_ADMIN for later phases.

---

## Phase 2 — Verification / KYC  *(verification-service :8082)* — unlocks trading

| Screen | Endpoints | Roles |
|--------|-----------|-------|
| Submit KYC document (fileUrl + docType) | `POST /companies/{companyId}/verifications` | COMPANY_ADMIN |
| My company's verifications list + status | `GET /companies/{companyId}/verifications` | authenticated |
| Verification detail | `GET /companies/{companyId}/verifications/{id}` or `GET /verifications/{id}` | authenticated |

> Note: real file upload is deferred backend-side — `fileUrl` is a string today. UI sends a
> URL/placeholder; swap to a real uploader when `StorageService` lands.

**Definition of working:** as COMPANY_ADMIN submit a KYC doc → it appears `PENDING` in the
list. (Approval happens in Phase 6 admin.)

---

## Phase 3 — Catalog & Sourcing  *(catalog :8083, sourcing :8084)*

Ties into the marketing "HS-code categories" story.

| Screen | Endpoints | Roles |
|--------|-----------|-------|
| HS-code browser / lookup | `GET /hs-codes`, `GET /hs-codes/{code}` | authenticated |
| Product catalog (search by hsCode) | `GET /products?hsCode=` | authenticated |
| Product detail | `GET /products/{id}` | authenticated |
| My products: create / edit / delete (seller) | `POST/PUT/DELETE /products`, `GET /products/{id}` | COMPANY_ADMIN/MEMBER |
| RFQ list + create (buyer) | `GET/POST /rfqs` | COMPANY_ADMIN/MEMBER |
| RFQ detail + **matches** | `GET /rfqs/{id}`, `GET /rfqs/{id}/matches` | authenticated |

**Definition of working:** seller creates a product under HS code X → buyer posts an RFQ for
HS code X → RFQ detail's "matches" shows the seller's product (proves the cross-service
catalog lookup works).

---

## Phase 4 — Deal flow  *(quotation :8085, messaging :8086, orders :8087, documents :8088)*

The core marketplace loop. Best demoed with two browser sessions (buyer + seller).

| Screen | Endpoints | Roles |
|--------|-----------|-------|
| Quotes against an RFQ (submit/list) | `POST /quotes`, `GET /quotes?rfqId=` | COMPANY_ADMIN/MEMBER |
| Quote detail + accept / reject / counter | `GET /quotes/{id}`, `POST /quotes/{id}/accept|reject|counter` | COMPANY_ADMIN/MEMBER |
| Conversations list + thread | `GET/POST /conversations`, `GET/POST /conversations/{id}/messages` | COMPANY_ADMIN/MEMBER |
| Orders list + detail | `GET /orders`, `GET /orders/{id}` | authenticated |
| Order status update | `PATCH /orders/{id}/status` | COMPANY_ADMIN/MEMBER |
| Documents for an order (generate/list/view) | `POST/GET /documents`, `GET /documents?orderId=` | COMPANY_ADMIN/MEMBER |

**Definition of working:** seller submits a quote on the buyer's RFQ → buyer accepts →
an **order is auto-created** (idempotent on quoteId) and shows in Orders → generate a
commercial invoice document for that order → buyer & seller exchange chat messages.

---

## Phase 5 — Execution  *(logistics :8089, payments :8090)*

| Screen | Endpoints | Roles |
|--------|-----------|-------|
| Logistics partners (reference) | `GET /logistics-partners` | authenticated |
| Book shipment for an order | `POST /shipments` | COMPANY_ADMIN/MEMBER |
| Shipment list + tracking timeline | `GET /shipments?orderId=`, `GET /shipments/{id}` | authenticated |
| Add tracking event | `POST /shipments/{id}/events` | COMPANY_ADMIN/MEMBER |
| Escrow: initiate / fund / release / refund | `POST /payments`, `POST /payments/{id}/fund|release|refund` | release/refund: COMPANY_ADMIN |
| Payment detail + status | `GET /payments/{id}`, `GET /payments` | authenticated |
| Letters of credit + payment terms | `POST/GET /letters-of-credit`, `GET /payment-terms` | mixed |

**Definition of working:** for an order → book a shipment, add 2–3 tracking events that march
the timeline forward → initiate escrow (amount snapshotted from the order) → fund → release →
status reflects each transition.

---

## Phase 6 — Revenue & operations  *(billing :8091, trust :8092, notification :8093, admin :8094)*

| Screen | Endpoints | Roles |
|--------|-----------|-------|
| Plans + subscribe (link from marketing Pricing) | `GET /plans`, `POST /subscriptions` | COMPANY_ADMIN |
| My subscription + cancel | `GET /subscriptions?companyId=`, `POST /subscriptions/{id}/cancel` | COMPANY_ADMIN |
| Platform payments | `GET /platform-payments`, `POST /platform-payments/{id}/pay` | COMPANY_ADMIN |
| Ratings (give/list) + company summary | `POST/GET /ratings`, `GET /ratings/summary?ratedCompanyId=` | COMPANY_ADMIN/MEMBER |
| Disputes (raise + track) | `POST/GET /disputes` | COMPANY_ADMIN/MEMBER |
| Notification bell + list + mark read | `GET /notifications?unread=`, `POST /notifications/{id}/read` | authenticated |
| **Admin console:** KYC approve/reject | `POST /admin/verifications/{id}/approve|reject` | PLATFORM_ADMIN |
| **Admin:** sanctions screen + audit log + metrics dashboard | `POST /admin/sanctions/screen`, `GET /admin/sanctions|actions|metrics`, dispute `PATCH /disputes/{id}/status` | PLATFORM_ADMIN / SUPPORT |

**Definition of working:** subscribe a company to the "Growth" plan → PLATFORM_ADMIN approves
the Phase-2 KYC submission → buyer rates the seller and the summary updates → a notification
appears in the bell → admin metrics dashboard shows non-zero counts.

---

## Cross-cutting (applies to every phase)

- **Auth header forwarding** is automatic via the interceptor; cross-service reads on the
  backend already forward the JWT.
- **Token expiry (15 min):** handle 401/403 globally → re-login. No refresh token yet.
- **Role-driven nav:** hide/disable actions the caller's role can't perform (mirror the
  `@PreAuthorize` matrix) — but still rely on the backend as source of truth (403 handled).
- **Empty / loading / error states** for every list and detail screen (Phase 0 primitives).
- **Pagination** is zero-indexed to match `PageResponse`.
- **Known backend gaps to design around:** no file upload (KYC `fileUrl` is a string),
  no WebSocket (messaging is poll/refresh REST), no company-ownership checks yet
  (any COMPANY_ADMIN can act on any company id), document is a `fileUrl` placeholder.

## Test data you'll need
- **Company A (buyer)** — COMPANY_ADMIN + member
- **Company B (seller)** — COMPANY_ADMIN + member
- **One PLATFORM_ADMIN** (for KYC approval, sanctions, metrics)
Run the stack with `./run-all.sh` (or per-service `spring-boot:run`) + `docker compose up -d`
for Mongo, sharing one `JWT_SECRET` and `MONGODB_URI`.

## Suggested delivery order
Phase 0 → 1 → 2 → 3 → 4 → 5 → 6. Phases 1–4 are the critical path for a working demo;
5 and 6 layer on execution and operations. Each phase is independently demoable.
