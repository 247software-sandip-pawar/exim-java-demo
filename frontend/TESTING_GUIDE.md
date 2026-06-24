# Hirkani Exim — App Testing Guide

A click-by-click walkthrough to verify every screen against the running backend.
Follow it top to bottom; later phases reuse data created in earlier ones.

---

## 0. URLs & prerequisites

| What | URL |
|------|-----|
| **Web app (open this)** | **http://localhost:5173** |
| Marketing site | http://localhost:5173/ |
| App (after login) | http://localhost:5173/app |
| API gateway (proxied as `/api`) | http://localhost:8080 |

The Vite dev server proxies `/api → :8080`, so no CORS setup is needed.

**Backend must be running** — gateway `:8080` + all 14 services `:8081–8094` (and MongoDB).
Check: `curl http://localhost:8080/actuator/health` → `{"status":"UP"}`.

> **JWT lifetime is 15 minutes** and there's no refresh token. If actions start
> returning "session expired", just log in again.

---

## Test data you'll create (do Phase 1 first)

| Account | Role | Purpose |
|---------|------|---------|
| **Company A — Buyer** | COMPANY_ADMIN | posts RFQs, accepts quotes, raises disputes |
| **Company B — Seller** | COMPANY_ADMIN | lists products, submits quotes |
| **Platform admin** | PLATFORM_ADMIN | KYC approval, sanctions, metrics (see note) |

> **Getting a PLATFORM_ADMIN:** self-registration always creates a COMPANY_ADMIN.
> A platform admin must already exist in the DB (or be created by another admin via
> **Users → New user**). Use those credentials for the Admin-console steps (Phase 6).

**Tip:** test the buyer↔seller flows with **two browsers** (or one normal + one
incognito window) logged in as Company A and Company B side by side.

---

## Phase 1 — Identity & Auth  *(the gate)*

1. **Register Company A (buyer)** — go to `/register`, fill company name, type
   `IMPORTER`, country, admin name, email, password (≥ 8 chars) → **Create account**.
   - ✅ You're redirected into `/app`; the topbar shows your name + company.
2. **Refresh the page.** ✅ You stay logged in (token persisted).
3. **Log out** (topbar user menu) → ✅ back to login; protected routes redirect to `/login`.
4. **Register Company B (seller)** the same way (type `EXPORTER`) in a second browser.
5. **Account → Company** (`/app/company`). ✅ Shows your company profile.
6. **(Platform admin only)** **Users** + **Companies** appear in the sidebar;
   list/create/edit/delete work.

**Working when:** register → auto-login → refresh keeps you in → logout clears it.

---

## Phase 2 — Verification / KYC

1. As **Company A**, go to **Account → Verification** (`/app/verification`).
2. **Submit document** → pick a type (IEC / GST / RCMC / BANK), paste any URL in
   `fileUrl` (e.g. `https://example.com/iec.pdf`) → **Submit**.
   - ✅ A row appears with status **PENDING**.
3. Note: approval happens in **Phase 6 → Admin console**.

> Real file upload is a known backend gap — `fileUrl` is just a string today.

**Working when:** a submitted KYC doc shows as PENDING in the list.

---

## Phase 3 — Catalog & Sourcing

1. As **Company B (seller)** → **Trade → HS Codes** (`/app/hs-codes`): browse/lookup.
   Note a code (e.g. `0901.21`).
2. **Trade → Catalog** → **New product**: name, that HS code, unit price, currency
   (3-letter), unit → **Save**. ✅ Product appears; open it to see detail; Edit/Delete
   work (only on your own products).
3. As **Company A (buyer)** → **Trade → Sourcing / RFQs** → **New RFQ**: title, the
   **same HS code**, quantity, unit, currency → **Post RFQ**.
4. Open the RFQ → **Matching products** section.
   - ✅ Company B's product appears (proves the cross-service catalog lookup).

**Working when:** seller's product shows up under the buyer's RFQ matches.

---

## Phase 4 — Deal flow  *(best with two browsers)*

1. As **Company B (seller)**, open the buyer's RFQ (Sourcing → the RFQ).
   In the **Quotes** section → **Submit quote**: pick the matching product, set qty /
   unit price / incoterm → **Submit quote**.
   - ✅ Quote appears in the RFQ's Quotes list and under **Trade → Quotes**.
2. As **Company A (buyer)**, open that quote (**Trade → Quotes → row**, or from the RFQ).
   - **Counter** (optional): change terms → a new linked quote is created.
   - **Accept** → ✅ status becomes **ACCEPTED**.
3. Still on the accepted quote → **Create order**.
   - ✅ Redirects to the new order under **Trade → Orders** (creating again is
     idempotent — same order).
4. **Orders → open the order** → **Advance status** (e.g. `CREATED → CONFIRMED`).
   Only valid next states are offered.
5. On the order, **Documents** section → **Generate document** → pick
   *Commercial invoice* → ✅ a document with a number (e.g. `CI-…`) appears.
   Also visible under **Trade → Documents**.
6. **Trade → Messages → New conversation**: subject + pick the counterparty company →
   send messages from both sides. ✅ Thread updates (polls every ~5s); an in-chat
   **Offer** renders if present.

**Working when:** quote → accept → auto-order → invoice generated → buyer & seller chat.

---

## Phase 5 — Execution

1. **Execution → Shipments → Book shipment**: pick the order, a logistics partner,
   transport mode, origin, destination → **Book shipment**.
   - ✅ Shipment created with a tracking number; status **CREATED**.
2. Open the shipment → **Add tracking event**, marching it forward:
   `BOOKED → IN_TRANSIT → ARRIVED → DELIVERED` (only valid next states offered).
   - ✅ The **tracking timeline** grows with each event.
3. **Execution → Payments** (three tabs):
   - **Escrow → Initiate escrow**: pick the order, method `ESCROW`, optional term →
     ✅ transaction created (amount **snapshotted from the order**), status **INITIATED**.
   - Open it → **Fund** (→ FUNDED) → **Release** (→ RELEASED).
     *(Release/Refund are COMPANY_ADMIN-only.)*
   - **Letters of credit → Open letter of credit**: beneficiary, issuing bank, amount →
     ✅ LC created (`LC-…`, DRAFT). Click it → **Update status** `DRAFT → ISSUED`.
   - **Payment terms**: ✅ seeded reference list (NET_30, etc.).

**Working when:** shipment timeline advances and escrow goes INITIATED→FUNDED→RELEASED.

---

## Phase 6 — Revenue & operations

### Business (any COMPANY_ADMIN)
1. **Business → Subscription** (`/app/billing`): plans grid → **Subscribe** to one.
   - ✅ "Current plan" card shows ACTIVE; **Cancel subscription** works.
   - Platform-payment invoices list below; **Pay now** appears on PENDING ones.
2. **Business → Ratings**: **Rate a company** → pick order + counterparty + score →
   ✅ summary (avg stars + count) and the list update for that company.
3. **Business → Disputes**: **Raise dispute** → pick order + against-company + reason →
   ✅ appears as **OPEN**. (Advancing it needs admin/support — next section.)

### Notifications (everyone)
4. Topbar **bell** 🔔: shows unread dot/count, lists recent notifications, and the
   check icon **marks one read**. (Polls every ~20s.)

### Admin console (PLATFORM_ADMIN only — `/app/admin`)
5. **Metrics** tab: ✅ stat cards (sanctioned entities, admin actions, KYC approvals…).
6. **KYC review** tab: paste the **verification ID** from Phase 2 → **Look up** →
   **Approve** (or Reject) with a note → ✅ status flips to APPROVED.
   *(Find the ID on the company's Verification page row / detail.)*
7. **Sanctions** tab: type a name → **Screen** → ✅ hit/no-hit result; seeded
   denied-party list shows below.
8. **Audit log** tab: ✅ your KYC decision + sanction screen appear as rows.
9. **Disputes** (from step 3): as admin/support, open the dispute → **Update status**
   `OPEN → UNDER_REVIEW → RESOLVED` (with a resolution note).

**Working when:** subscribe → admin approves the Phase-2 KYC → rating summary updates →
a notification shows in the bell → admin metrics show non-zero counts.

---

## Cross-cutting checks (any screen)

- **Loading / empty / error states** render on every list & detail.
- **Pagination** on long lists (zero-indexed, matches the backend).
- **Role-gated actions** are hidden when your role can't perform them — but the backend
  is the source of truth (a 403 is handled with a toast).
- **Toasts** confirm successes and surface errors.

---

## Known backend gaps (by design — not bugs)

- KYC `fileUrl` is a string (no real upload yet).
- Messaging is REST polling (no WebSocket).
- Documents store a `fileUrl` placeholder (no real PDF rendering).
- Payments/LC are a guarded state machine with an `externalRef` placeholder (no
  licensed payment partner / signed webhooks).
- Notifications are in-app only (no email/push).
- Admin metrics are from admin-owned data; sanctions list is a seeded demo list.
- No per-resource company-ownership checks yet (any COMPANY_ADMIN can act on any id).
