# EXIM Marketplace — iOS App

Native SwiftUI client for the EXIM Marketplace microservices platform. Talks to the API
gateway (`http://localhost:8080`) — the same REST surface the web frontend uses.

- **Minimum target:** iOS 18.0 · iPhone + iPad (universal, `TARGETED_DEVICE_FAMILY 1,2`)
- **UI:** SwiftUI with a frosted-glass ("glass UI") design over a clean Instagram-like theme —
  system backgrounds, one accent blue, story-ring gradient for highlights, `.ultraThinMaterial`
  cards/chips/bars. On iPad the tab bar automatically becomes a sidebar
  (`.tabViewStyle(.sidebarAdaptable)`).
- **Language mode:** Swift 6 (strict concurrency), `@Observable` view models, async/await.

## Open & run

```bash
open ios/EximMarketplace/EximMarketplace.xcodeproj
```

1. Start the backend: run MongoDB (`docker compose up -d` at repo root or Atlas), then start the
   services and the gateway (`mvn -pl gateway spring-boot:run`, etc. — see the root `CLAUDE.md`).
2. Select the **EximMarketplace** scheme and an iPhone or iPad simulator, then Run.
3. Register a company from the app's **Create Company Account** screen (calls
   `POST /api/v1/auth/register`), or sign in with an existing user.

**Physical device?** The simulator can reach `localhost`, a device cannot. In the app go to
**Home → avatar → Profile → API Server** and set the base URL to your Mac's LAN IP, e.g.
`http://192.168.1.10:8080` (ATS allows local networking via `Info.plist`).

**Note:** the backend issues 15-minute JWTs with no refresh token; when the token expires the
app signs you out and returns to the login screen.

## Architecture (clean architecture, SPM module per layer)

Everything except the thin app shell lives in the local Swift package
`Packages/EximKit`, one target per module:

```
EximMarketplace/            app shell (@main, assets, Info.plist)
Packages/EximKit/Sources/
  CoreKit/                  ApiResponse/Page envelopes, AppError, JSON coding, pagination driver
  NetworkingKit/            APIClient (URLSession, Bearer JWT), Endpoint, Keychain token store
  DomainKit/                entities + repository protocols (the "ports"; no networking imports)
  DataKit/                  Remote*Repository — REST adapters implementing DomainKit ports
  DesignSystemKit/          Theme, glass cards/buttons/chips, status badges, avatars, formatters
  SessionKit/               SessionStore (auth state; token in Keychain, user cached)
  AuthFeature/              landing, login, company registration
  HomeFeature/              dashboard, notifications, profile (+ API server setting, sign out)
  CatalogFeature/           products list/detail/create/edit, HS-code filter
  SourcingFeature/          RFQ list/detail/create + catalog matches → submit quote
  QuotationFeature/         quote list/detail, accept / reject / counter, create order from quote
  OrdersFeature/            order list/detail, status timeline + server-validated transitions
  MessagingFeature/         conversations, chat, start conversation
  AppFeature/               composition root: AppDependencies (DI), RootView, adaptive tab/sidebar
```

Dependency rule: features depend only on `DomainKit` protocols + `DesignSystemKit` + `SessionKit`;
`AppFeature` is the only module that sees `DataKit` and wires the REST implementations in
`AppDependencies`. Swap `DataKit` for mocks by injecting different implementations there.

## Covered API flows

Auth (register/login/me) · catalog products + HS codes · sourcing RFQs + matches · quotes
(submit/accept/reject/counter) · orders (create from accepted quote, status state machine) ·
conversations + messages · in-app notifications · companies (read).

Not yet covered (backend exists): verification/KYC, documents, logistics, payments, billing,
trust, admin.

## Tests

```bash
cd ios/EximMarketplace/Packages/EximKit
xcodebuild test -scheme EximKit-Package -destination 'platform=iOS Simulator,name=iPad (A16)'
```

Swift Testing suites cover envelope/date/decimal decoding and the order-status state machine
(kept in lockstep with `orders-service`).
