# Deep Dive: How a Module Works — Walkthrough of `quotation-service`

This single document explains, in detail, **how one service is built and how a request travels
through it**, using the **quotation** service as a worked example. Read this together with
`PROJECT_OVERVIEW.md` (the high-level picture). By the end you will be able to explain any service
in this project, because **every service follows the exact same shape**.

We use quotation because it exercises *everything*:
- all five layers (controller → service → repository → domain, plus DTOs),
- input validation and role-based security,
- a **cross-service call** (it asks the identity service "does this company exist?"),
- real **business rules** (a quote can be submitted, accepted, rejected, or countered), and
- reading/writing its **own database** (`exim_quotation`).

---

## Part A — The mental model (1 minute)

A service is a small office. A request comes in the front door and passes through five desks:

```
   HTTP request                                              HTTP response
        │                                                          ▲
        ▼                                                          │
  ┌───────────┐    ┌──────────┐    ┌────────────┐    ┌──────────┐ │
  │ CONTROLLER │──►│ SERVICE   │──►│ REPOSITORY  │──►│ DATABASE  │ │
  │ reception  │   │ manager   │   │ filing clerk│   │ exim_     │ │
  │ (web)      │◄──│ (rules)   │◄──│ (data)      │◄──│ quotation │ │
  └───────────┘    └────┬─────┘    └────────────┘    └──────────┘ │
        │               │  uses DOMAIN objects (the records) ─────┘
        │               └──► may CALL ANOTHER SERVICE over the web (identity)
        │
        └──► converts to/from DTOs (the public "forms") on the way in and out
```

| Layer | Role | In quotation-service |
|-------|------|----------------------|
| **Controller** | Front desk: receive request, validate form, check role, return reply | `QuoteController.java` |
| **Service** | Manager: the business rules | `QuoteService.java` |
| **Repository** | Filing clerk: the only code that touches the database | `QuoteRepository.java` |
| **Domain** | The official record stored in the DB | `Quote.java`, `QuoteStatus.java` |
| **DTO** | The public forms (in/out); we never expose the raw record | `QuoteRequest`, `CounterQuoteRequest`, `QuoteResponse` |

Supporting cast (shared across all services, defined once):
- **Security** (`libs/security`): checks the JWT token on every request.
- **Clients** (`libs/common`): the "phone" to call other services (here, `IdentityClient`).
- **Envelope** (`libs/common`): every reply is wrapped in a standard `{success,data,error,timestamp}`.

---

## Part B — What this module is responsible for

A **Quote** is a seller's price offer against a buyer's request (RFQ). The buyer can then act on it.
The whole life of a quote is a small **state machine**:

```
                    ┌────────────► REJECTED   (buyer says no)
                    │
   (seller) ──► SUBMITTED ─────────► ACCEPTED   (buyer says yes → becomes an order later)
                    │
                    └────────────► COUNTERED   (buyer proposes new terms)
                                        │
                                        └──► creates a NEW quote (SUBMITTED) linked to the old one
```

Rule that matters: **you can only act on a `SUBMITTED` quote.** Trying to accept an already-accepted
quote is rejected with a clear business error (not a crash, not a silent no-op).

The HTTP endpoints (all under `/api/v1/quotes`):

| Action | Method + path | Who | Result |
|--------|---------------|-----|--------|
| Submit a quote | `POST /api/v1/quotes` | seller | new quote, status `SUBMITTED` |
| Get one quote | `GET /api/v1/quotes/{id}` | any logged-in user | the quote |
| List quotes | `GET /api/v1/quotes?rfqId=…&status=…` | any logged-in user | a page of quotes |
| Accept | `POST /api/v1/quotes/{id}/accept` | buyer | status → `ACCEPTED` |
| Reject | `POST /api/v1/quotes/{id}/reject` | buyer | status → `REJECTED` |
| Counter | `POST /api/v1/quotes/{id}/counter` | buyer | old → `COUNTERED`, new quote created |

---

## Part C — Follow ONE request through every layer

### Scenario: a seller submits a quote

**The HTTP call** (this is what a frontend or `curl` sends):

```http
POST http://localhost:8080/api/v1/quotes          ← note: through the GATEWAY (port 8080)
Authorization: Bearer eyJhbGciOiJIUzI1Ni␣...       ← the login token (proves who you are + role)
Content-Type: application/json

{
  "rfqId":           "1f0a...buyer's request id...",
  "sellerCompanyId": "77c3...seller company id...",
  "buyerCompanyId":  "2b9e...buyer company id...",
  "productId":       "9a01...catalog product id...",
  "productName":     "Arabica Green Coffee",
  "quantity":        500,
  "unit":            "kg",
  "unitPrice":       12.50,
  "currency":        "USD",
  "incoterm":        "FOB"
}
```

#### Step 0 — The Gateway (port 8080)
The request hits the **gateway** first. It looks at the path `/api/v1/quotes/**` and forwards it to
the quotation service on port 8085. The gateway does **routing only** — no business logic.

```yaml
# gateway/src/main/resources/application.yml
- id: quotation
  uri: ${QUOTATION_URI:http://localhost:8085}
  predicates:
    - Path=/api/v1/quotes/**
```

#### Step 1 — Security filter (shared, runs before the controller)
Before any of our code runs, the shared `JwtAuthenticationFilter` reads the `Authorization` header,
validates the token with the shared `JWT_SECRET`, and records "who you are + your role". If the
token is missing/expired, the request continues *unauthenticated* and will be rejected at the next
step.

```java
// libs/security/JwtAuthenticationFilter.java (simplified)
String token = header.substring(7);              // strip "Bearer "
Claims claims = jwtService.parse(token);          // verify signature + expiry
String role = claims.get("role", String.class);   // e.g. COMPANY_MEMBER
// → puts ROLE_COMPANY_MEMBER into the security context for this request
```

#### Step 2 — Controller (the receptionist)
`QuoteController` receives the request. Three things happen automatically/declaratively:
- `@Valid` checks the incoming form against its rules (e.g. price not null, quantity ≥ 1).
- `@PreAuthorize` checks the caller's role.
- If both pass, it calls the service and wraps the result in the standard envelope.

```java
// quotation/controller/QuoteController.java
@PostMapping
@ResponseStatus(HttpStatus.CREATED)                                   // returns HTTP 201
@PreAuthorize("hasAnyRole('COMPANY_ADMIN', 'COMPANY_MEMBER', 'PLATFORM_ADMIN')")
public ApiResponse<QuoteResponse> submit(@Valid @RequestBody QuoteRequest request) {
    return ApiResponse.ok(quoteService.submit(request));              // hand off to the manager
}
```

The **incoming DTO** defines the form + its validation rules:

```java
// quotation/dto/QuoteRequest.java (excerpt)
@NotNull   private UUID rfqId;
@NotNull   private UUID sellerCompanyId;
@NotNull   private UUID buyerCompanyId;
@NotNull   private UUID productId;
@Min(1)    private int quantity;          // quantity must be at least 1
@NotNull   private BigDecimal unitPrice;
@NotBlank  private String currency;
```

If, say, `quantity` were 0, the request never reaches the service — the user gets a clean
`400 VALIDATION_FAILED` response automatically (handled centrally in `GlobalExceptionHandler`).

#### Step 3 — Service (the manager — the real work)
`QuoteService.submit(...)` runs the business logic:

```java
// quotation/service/QuoteService.java (the submit method)
public QuoteResponse submit(QuoteRequest req) {
    // 3a. CROSS-SERVICE CHECK: ask the identity service if these companies exist.
    if (!identityClient.companyExists(req.getSellerCompanyId()))
        throw new NotFoundException("Seller company not found.");
    if (!identityClient.companyExists(req.getBuyerCompanyId()))
        throw new NotFoundException("Buyer company not found.");

    // 3b. Build the official record (domain object) from the form.
    Quote quote = new Quote();
    quote.setRfqId(req.getRfqId());
    quote.setSellerCompanyId(req.getSellerCompanyId());
    quote.setBuyerCompanyId(req.getBuyerCompanyId());
    quote.setProductId(req.getProductId());
    quote.setProductName(req.getProductName());
    quote.setQuantity(req.getQuantity());
    quote.setUnit(req.getUnit());
    quote.setUnitPrice(req.getUnitPrice());
    quote.setCurrency(req.getCurrency());
    quote.setIncoterm(req.getIncoterm());
    quote.setStatus(QuoteStatus.SUBMITTED);                 // every new quote starts here

    // 3c. Save it, then convert to the OUTGOING form and return.
    return QuoteResponse.from(quoteRepository.save(quote));
}
```

**Step 3a is the cross-service call.** Quotation does **not** open identity's database. It makes a
small web call, *carrying the same user token*, via a shared helper:

```java
// libs/common/client/IdentityClient.java (simplified)
public boolean companyExists(UUID companyId) {
    return restClient.get()
        .uri("/api/v1/companies/{id}", companyId)
        .headers(this::forwardAuthorization)     // forwards the caller's Bearer token
        .exchange((req, res) -> res.getStatusCode().value() == 200);  // 200 = exists, 404 = not
}
```

```
  quotation-service  ───── "GET /api/v1/companies/{id}" (with token) ─────►  identity-service
                     ◄──────────── 200 OK (exists) / 404 (missing) ──────────
```

#### Step 4 — Domain (the official record)
`Quote` is the thing that actually gets stored. It extends a shared `BaseEntity` that gives every
record an **id**, a **version** (for safe concurrent updates), and **created/updated timestamps**.

```java
// quotation/domain/Quote.java (shape)
@Document(collection = "quotes")          // stored in the "quotes" collection
public class Quote extends BaseEntity {   // id, version, createdAt, updatedAt come from BaseEntity
    @Indexed private UUID rfqId;          // @Indexed = fast lookups by this field
    @Indexed private UUID sellerCompanyId;
    @Indexed private UUID buyerCompanyId;
    private UUID productId;
    private String productName;
    private int quantity;
    private String unit;
    private BigDecimal unitPrice;
    private String currency;
    private String incoterm;
    private QuoteStatus status = QuoteStatus.SUBMITTED;
    private UUID parentQuoteId;            // set only when this quote is a counter-offer
}
```

#### Step 5 — Repository (the filing clerk)
The repository is a one-line interface; the framework provides the actual save/find/list code. This
is the **only** layer allowed to touch the database.

```java
// quotation/repository/QuoteRepository.java
public interface QuoteRepository extends MongoRepository<Quote, UUID> {
    Page<Quote> findByRfqId(UUID rfqId, Pageable pageable);
    Page<Quote> findByStatus(QuoteStatus status, Pageable pageable);
    Page<Quote> findByRfqIdAndStatus(UUID rfqId, QuoteStatus status, Pageable pageable);
}
```

#### Step 6 — What lands in the database
A new document appears in the `quotes` collection of the `exim_quotation` database:

```json
{
  "_id":             "c4e2...newly-generated-id...",
  "rfqId":           "1f0a...",
  "sellerCompanyId": "77c3...",
  "buyerCompanyId":  "2b9e...",
  "productId":       "9a01...",
  "productName":     "Arabica Green Coffee",
  "quantity":        500,
  "unit":            "kg",
  "unitPrice":       "12.50",
  "currency":        "USD",
  "incoterm":        "FOB",
  "status":          "SUBMITTED",
  "parentQuoteId":   null,
  "version":         0,
  "createdAt":       "2026-06-23T00:00:00Z",
  "updatedAt":       "2026-06-23T00:00:00Z"
}
```

#### Step 7 — The response (outgoing DTO + envelope)
The service mapped the saved record to a clean **outgoing form** (`QuoteResponse.from(quote)`), and
the controller wrapped it in the standard envelope. The caller receives:

```json
HTTP 201 Created
{
  "success": true,
  "data": {
    "id":        "c4e2...",
    "rfqId":     "1f0a...",
    "productName": "Arabica Green Coffee",
    "quantity":  500,
    "unitPrice": 12.50,
    "currency":  "USD",
    "status":    "SUBMITTED",
    "createdAt": "2026-06-23T00:00:00Z"
  },
  "error": null,
  "timestamp": "2026-06-23T00:00:00Z"
}
```

---

## Part D — A second flow that shows the business rules: "accept" and "counter"

### Accept (`POST /api/v1/quotes/{id}/accept`)
The manager enforces the state-machine rule before changing anything:

```java
public QuoteResponse accept(UUID id) {
    Quote quote = requireSubmitted(id);          // 404 if missing; 422 if not SUBMITTED
    quote.setStatus(QuoteStatus.ACCEPTED);
    return QuoteResponse.from(quoteRepository.save(quote));   // explicit save (see note below)
}

private Quote requireSubmitted(UUID id) {
    Quote quote = quoteRepository.findById(id)
        .orElseThrow(() -> new NotFoundException("Quote not found."));
    if (quote.getStatus() != QuoteStatus.SUBMITTED)
        throw new BusinessException("QUOTE_NOT_ACTIONABLE",
            "Quote is " + quote.getStatus() + "; only a SUBMITTED quote can be acted on.");
    return quote;
}
```

- Accept an already-accepted quote → clean `422` business error, nothing changes.
- Accept a non-existent quote → clean `404`.

> **Important detail (MongoDB has no auto-save).** After changing a record we must call
> `repository.save(...)` explicitly — unlike some databases, the change is **not** written
> automatically when the method ends. This is a deliberate convention across the whole project.

### Counter (`POST /api/v1/quotes/{id}/counter`)
A counter-offer marks the old quote `COUNTERED` and creates a **new** `SUBMITTED` quote that points
back to the old one (`parentQuoteId`), so the negotiation history is preserved:

```java
public QuoteResponse counter(UUID id, CounterQuoteRequest req) {
    Quote original = requireSubmitted(id);
    original.setStatus(QuoteStatus.COUNTERED);
    quoteRepository.save(original);                 // save the old one

    Quote counter = new Quote();
    // ...copies rfq/buyer/seller/product from the original, applies the new price/qty...
    counter.setStatus(QuoteStatus.SUBMITTED);
    counter.setParentQuoteId(original.getId());     // link new → old
    return QuoteResponse.from(quoteRepository.save(counter));   // save the new one
}
```

---

## Part E — How this module connects to the rest of the deal

```
  sourcing (RFQ)          identity (companies)
      ▲                        ▲
      │ rfqId reference        │ "does this company exist?" (REST call, with token)
      │                        │
   ┌──┴────────────────────────┴──┐
   │        QUOTATION              │   buyer ACCEPTS a quote
   │  submit / accept / reject /   │────────────────────────────►  ORDERS
   │  counter                      │   (orders reads the accepted   creates ONE order
   └───────────────────────────────┘    quote via QuotationClient)  per quote
```

- Quotation **reads** from identity (company existence) — over REST, with the user's token.
- Quotation **does not** call orders. Instead, when a quote is accepted, the **orders** service
  later *pulls* the accepted quote from quotation (using `QuotationClient.getQuote(id)`) and builds
  an order. This keeps each service independent and the dependencies one-directional.

---

## Part F — How it's configured and tested

**Configuration** (`services/quotation-service/src/main/resources/application.yml`) — only non-secret
settings live here; secrets come from the environment:

```yaml
spring:
  application: { name: quotation-service }
  mongodb:
    uri:      ${MONGODB_URI:mongodb://localhost:27017/?directConnection=true}  # secret from env
    database: ${QUOTATION_DB_NAME:exim_quotation}                              # its OWN database
app:
  jwt:
    secret: ${JWT_SECRET:change-me-...}        # shared signing key, from env
  clients:
    identity:
      base-url: ${IDENTITY_BASE_URL:http://localhost:8081}   # where to reach identity
server:
  port: 8085                                   # this service's port
```

The two **secrets** (`MONGODB_URI`, `JWT_SECRET`) come from `set-env.sh`, never from this file.
(See `PROJECT_OVERVIEW.md` §8.)

**Tests** (`QuoteServiceTest.java`) check the business rules with *fake* dependencies (no real
database, no real identity service), so they run in milliseconds. Examples that exist today:
- submit succeeds when both companies exist; fails (and saves nothing) when the seller is missing;
- accept moves `SUBMITTED → ACCEPTED`; accept on a resolved quote throws the business error;
- counter marks the original `COUNTERED` and creates a linked child.

---

## Part G — The 6 takeaways to explain this module to anyone

1. **One responsibility:** quotation owns the life of a price offer (submit → accept/reject/counter).
2. **Five layers, always the same:** Controller (front desk) → Service (rules) → Repository (data)
   → Domain (record), with DTOs as the public forms.
3. **It guards its rules:** only a `SUBMITTED` quote can be acted on; bad input and bad states give
   clean, predictable errors — never crashes.
4. **It stays in its lane:** it reads other services over the web (with the user's token), never by
   touching their databases; and it owns exactly one database, `exim_quotation`.
5. **Secrets stay out of code:** the DB connection and the token key come from the environment file.
6. **Every other service in the project looks exactly like this** — so understanding quotation means
   understanding the whole codebase.
