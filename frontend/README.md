# Hirkani Exim — Web Frontend

Public marketing site for **hirkaniexim.com**, built with Vite + React + Tailwind +
shadcn/ui-style components. Themed to match the EXIM microservice backend.

## Run

```bash
cd frontend
npm install      # first time only
npm run dev      # http://localhost:5173
npm run build    # production build → dist/
npm run preview  # preview the production build
```

## Screens (routes)

| Route | Screen | Show the client |
|-------|--------|-----------------|
| `/` | **Home** | Hero, live deal card, stats, features, how-it-works, categories, testimonials, CTA |
| `/how-it-works` | **How it works** | 4-step timeline, platform capabilities, FAQ |
| `/pricing` | **Pricing** | Starter / Growth / Enterprise plans, pricing FAQ |
| `/about` | **About** | Brand story, mission/vision, values, stats |
| `/contact` | **Contact** | Contact channels + working demo form (success state) |

All content is mock data in `src/data/content.js` — swap for real API calls to the
gateway (`:8080`) when wiring the live backend.

## Structure

```
src/
  components/
    ui/         shadcn-style primitives (button, card, badge, accordion,
                input, label, dialog, dropdown-menu, spinner, skeleton)
    layout/     Navbar, Footer, Logo, PageHero, SectionHeading (marketing)
    app/        AppShell, Sidebar, Topbar, NotificationBell (authenticated shell)
    common/     DataTable, FormField, PageHeader, StatusBadge, Pagination,
                Loading/Error/Empty states (reused across all modules)
  auth/         AuthContext (login/register/logout, JWT), RouteGuards
  config/       nav.js (role-based sidebar nav, grouped by phase)
  hooks/        useApi.js (useApiQuery / useApiMutation over React Query)
  lib/          api.js (axios + interceptors + ApiResponse unwrap), queryClient.js
  pages/
    auth/       Login, Register, AuthLayout
    app/        DashboardHome, ComingSoon (placeholder for upcoming modules)
    (root)      Home, HowItWorks, Pricing, About, Contact (marketing)
  data/         content.js (mock marketing copy + plans)
  index.css     design tokens (brand colors as HSL CSS vars)
```

### Routes
- Public: `/`, `/how-it-works`, `/pricing`, `/about`, `/contact`
- Auth: `/login`, `/register`
- App (protected): `/app` (dashboard) + module routes under `/app/*`

### Phase 0 foundation (done)
Auth context + JWT storage, axios client that unwraps `ApiResponse`/handles 401,
React Query, role-aware app shell, and shared table/form/state components.
Module screens are wired phase-by-phase per `UI_BUILD_PLAN.md`.

See **DESIGN_GUIDELINES.md** for the full design system (color, type, spacing,
components, a11y, page inventory).

## Connecting to the backend (later)

The API gateway runs on `:8080`. Add a Vite dev proxy and a typed API client, then
replace the mock imports in `src/data/content.js` and the Contact form submit with
real calls (`/api/v1/...`). Responses are wrapped in `ApiResponse<T>`.
