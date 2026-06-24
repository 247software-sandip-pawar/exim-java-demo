# EXIM Marketplace — Project Overview (for non-deep-technical readers)

A plain-language guide to how this project is built, how the pieces talk to each other, how data
is stored, what each "layer" of code does, and what the environment file holds. Intended to be read
top-to-bottom and used to explain the system to a project manager.

---

## 1. What we are building (one paragraph)

EXIM Marketplace is a **B2B export–import platform**: sellers list products, buyers post requests,
they negotiate a price (a *quote*), the accepted quote becomes an *order*, and the platform
generates the *trade documents* (invoices etc.). Think of it as a guided marketplace for
international trade deals.

---

## 2. The big picture — one app made of small apps ("microservices")

Instead of one giant program, the system is split into **9 small programs**, each responsible for
one business area and each owning **its own database**. They all sit behind **one front door** (the
"gateway") so the outside world only ever talks to a single address.

```
                                 ┌─────────────────────────────────────────────┐
   Web / Mobile  ───────────►    │            GATEWAY  (port 8080)              │
   (one address)                 │   the single front door — routes traffic     │
                                 └───────┬───────────────────────────────┬──────┘
                                         │ (forwards each request to the right service)
        ┌────────────────────────────────┼────────────────────────────────┐
        ▼            ▼            ▼        ▼        ▼            ▼            ▼
   identity     verification   catalog  sourcing  quotation  messaging   orders   documents
   :8081          :8082        :8083    :8084     :8085      :8086       :8087    :8088
   (login,        (KYC docs)   (products)(buyer    (price     (buyer⇄    (orders) (invoices/
    companies,                          requests) offers)    seller chat)         trade docs)
    users)
        │            │            │        │        │            │           │         │
        ▼            ▼            ▼        ▼        ▼            ▼           ▼         ▼
   exim_identity  exim_       exim_     exim_     exim_       exim_       exim_     exim_
                 verification catalog   sourcing  quotation   messaging   orders    documents
                          (each service has its OWN separate database)
```

**Why split it up?** Each area can be developed, deployed, scaled, and fixed independently. A
problem in "messaging" can't break "orders." Teams can work in parallel.

**The golden rule:** a service **never reaches into another service's database**. If orders needs
quote information, it *asks* the quotation service over the web (a REST call) — it doesn't open
quotation's database. This keeps the boundaries clean.

| # | Service | Port | Business responsibility | Phase |
|---|---------|------|--------------------------|-------|
| 1 | identity | 8081 | Companies, users, login, issues the security token (JWT) | 1 ✅ |
| 2 | verification | 8082 | KYC document uploads & status | 2 ✅ |
| 3 | catalog | 8083 | Products & HS (customs) codes | 3 ✅ |
| 4 | sourcing | 8084 | RFQs (buyer requests) & matching to products | 3 ✅ |
| 5 | quotation | 8085 | Seller quotes: submit / accept / reject / counter | 4 ✅ |
| 6 | messaging | 8086 | Buyer⇄seller conversations & messages | 4 ✅ |
| 7 | orders | 8087 | Orders created from accepted quotes | 4 ✅ |
| 8 | documents | 8088 | Trade documents (invoices) for orders | 4 ✅ |
| — | gateway | 8080 | The single front door; routing only | ✅ |

Phases 5 (logistics + payments) and 6 (billing + ratings + notifications + admin) are still pending.

---

## 3. How a real deal flows through the system (the story)

This is the "happy path" — useful to narrate to a PM:

1. A company **registers and logs in** → *identity* gives them a security token (a digital wristband).
2. A seller **lists a product** → *catalog*.
3. A buyer **posts a request (RFQ)** → *sourcing*, which **matches** it to catalog products.
4. The seller **submits a quote** (price offer) → *quotation*. The buyer can **accept, reject, or
   counter** it.
5. On an **accepted quote**, an **order is created** → *orders*. (One order per quote — no
   duplicates, even if the request is sent twice.)
6. The platform **generates trade documents** (proforma/commercial invoice) for that order →
   *documents*.
7. Throughout, buyer and seller can **chat** → *messaging*.

Each arrow between services is a **web call carrying the user's security token**, so the whole chain
stays authenticated as the same person.

---

## 4. The layers inside ONE service (the "office" analogy)

Every service is built the same way, in **five layers**. Think of a single service as a small
office handling a request:

```
   HTTP request (e.g. "submit this quote")
        │
        ▼
   1. CONTROLLER   ── the RECEPTIONIST: takes the request, checks the form is filled in,
      (web layer)     checks the caller's role, hands it to the right department. Returns the reply.
        │
        ▼
   2. SERVICE      ── the MANAGER: the actual business rules live here.
      (logic)         ("only a SUBMITTED quote can be accepted", "an order needs an ACCEPTED quote").
        │              Talks to other services here if it needs outside info.
        ▼
   3. REPOSITORY   ── the FILING CLERK: the only one who opens the filing cabinet (database).
      (data access)    Save / find / list records. No business rules.
        │
        ▼
   4. DOMAIN       ── the OFFICIAL RECORD (what a "Quote" or "Order" actually is, stored in the DB).
      (entity)
```

Plus a fifth, cross-cutting layer:

```
   5. DTO          ── the PUBLIC FORM: the shape of data going in/out over the web.
   (request/response)  We never expose the raw internal record; we copy it into a clean public form.
```

**Worked example — "submit a quote" (`POST /api/v1/quotes`):**

| Layer | File (in quotation-service) | What it does |
|-------|-----------------------------|--------------|
| Controller | `QuoteController.java` | Receives the request, validates the form, checks the role, calls the service |
| DTO (in) | `QuoteRequest.java` | The incoming form (rfqId, price, quantity, …) with validation rules |
| Service | `QuoteService.java` | Checks buyer & seller exist (asks *identity*), creates the quote as `SUBMITTED` |
| Domain | `Quote.java` | The actual quote record stored in the database |
| Repository | `QuoteRepository.java` | Saves the quote into the `exim_quotation` database |
| DTO (out) | `QuoteResponse.java` | The clean reply sent back to the caller |

This same five-layer shape repeats in **every** service — once you understand one, you understand
all of them.

---

## 5. How services talk to each other

- They call each other over plain **web requests (REST)** — the same kind of call a browser makes.
- The caller's **security token is forwarded** on every internal call, so the downstream service
  knows who the user is and can enforce permissions.
- These "phone numbers" live in shared helper classes (in `libs/common`):
  - `IdentityClient` → "does this company exist?"
  - `CatalogClient` → "find products for this HS code"
  - `QuotationClient` → "give me this quote" (used by orders)
  - `OrdersClient` → "give me this order" (used by documents)
- If a downstream service is missing the data, the caller gets a clean "not found" — services don't
  crash each other.

---

## 6. How data is stored (the database setup)

- **Technology:** MongoDB (a document database) hosted on **MongoDB Atlas** (cloud).
- **One database per service**: `exim_identity`, `exim_catalog`, `exim_quotation`, etc. They live on
  the **same cluster** but are **separate databases** — no service can read another's.
- **No "schema" or migration scripts.** Unlike traditional SQL databases, collections (tables) and
  fields are created automatically the first time data is written. This means **no separate
  database-setup step** when adding a feature.
- **Reference data** (e.g. the starter list of HS customs codes) is loaded automatically on startup
  if the collection is empty.
- **Indexes** (for fast lookups and uniqueness rules, e.g. "one order per quote") are declared in
  the code and built automatically on startup.

---

## 7. Security (who is allowed to do what)

- **Login → token.** Only *identity* checks passwords. On successful login it issues a **JWT** — a
  signed token that proves who you are and your role. It currently lasts **15 minutes**.
- **Every service checks the token itself**, independently, using a shared secret. There is no
  central login server to call on every request — the token is self-contained, so it's fast.
- **Roles** decide permissions: `COMPANY_ADMIN`, `COMPANY_MEMBER`, `PLATFORM_ADMIN`, `SUPPORT`.
  Sensitive actions are tagged with the roles allowed to perform them.
- **Public by default-off:** every endpoint requires a valid token *except* a small allow-list
  (login, API documentation pages, health checks).
- The shared security code lives once in `libs/security` and is reused by all services.

---

## 8. The environment file (`set-env.sh`) — the project's two secrets

The system needs exactly **two secret values** to run. They are kept **out of the code** (and out of
version control) in a file called `set-env.sh`, which you "load" before starting the services.

```bash
# set-env.sh  (NOT committed to git — holds secrets)
export MONGODB_URI='mongodb+srv://<user>:<password>@<cluster>.mongodb.net/?appName=Cluster0'
export JWT_SECRET='<a long random secret string>'
```

| Variable | What it is | Why it matters |
|----------|------------|----------------|
| `MONGODB_URI` | The address + login for the MongoDB Atlas cluster | One shared cluster; **each service appends its own database name** internally, so they stay separated |
| `JWT_SECRET` | The shared signing key for security tokens | **Every service must use the same value** — otherwise a token issued by *identity* won't be trusted by *orders*. Keep it secret; anyone with it could forge tokens. |

Key points to tell the PM:
- These are **secrets** — never committed to git (the file is git-ignored), shared with the team
  out-of-band.
- **Same `JWT_SECRET` everywhere** is mandatory for services to trust each other.
- **One `MONGODB_URI` everywhere**, but data stays separated because each service writes to its own
  named database.
- For local development without the cloud, the code falls back to a local MongoDB, so a developer
  can run offline. For production, the real Atlas values are required.

Each service also has small config files (`application.yml`) that set non-secret things like its
**port number** and its **database name** — those are safe to commit.

---

## 9. How we build and run it

- **Build everything:** `mvn clean install` (compiles all 9 services + shared libraries and runs the
  automated tests).
- **Run everything locally:** `./run-all.sh` (builds, then starts all 9 services in the background);
  `./stop-all.sh` stops them. Logs go to `logs/`.
- **Automated tests:** each service has fast unit tests for its business rules that run with no
  database needed, so the build stays reliable.
- **API documentation:** each service auto-publishes interactive API docs (Swagger UI) at
  `http://localhost:<port>/swagger-ui.html`.

---

## 10. Status & what's next

| Phase | Scope | Status |
|-------|-------|--------|
| 0–1 | Setup + identity (login/companies/users) | ✅ Done |
| 2 | Verification (KYC) | ✅ Done |
| 3 | Catalog + sourcing | ✅ Done |
| 4 | Quotation + messaging + orders + documents | ✅ Done |
| 5 | **Logistics + payments** | ⏳ Pending |
| 6 | **Billing + ratings + notifications + admin** | ⏳ Pending |

Some advanced infrastructure is **intentionally deferred** so far (real-time chat sockets, automatic
PDF rendering, message queues, push notifications). The current build proves the full business flow
end-to-end with a clean, simple stack; those can be layered in when needed.

---

### One-line summaries to remember
- **9 small services, 1 front door, 1 database each.**
- **Five layers per service:** Controller → Service → Repository → Domain, with DTOs as the public forms.
- **Services talk over the web (REST), carrying the user's token; never across databases.**
- **Two secrets run the whole thing:** `MONGODB_URI` and `JWT_SECRET`, both in `set-env.sh`.
