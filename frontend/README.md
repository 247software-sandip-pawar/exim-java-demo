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
    ui/         shadcn-style primitives (button, card, badge, accordion)
    layout/     Navbar, Footer, Logo, PageHero, SectionHeading
  pages/        Home, HowItWorks, Pricing, About, Contact
  data/         content.js (mock copy + plans)
  lib/utils.js  cn() class merger
  index.css     design tokens (brand colors as HSL CSS vars)
```

See **DESIGN_GUIDELINES.md** for the full design system (color, type, spacing,
components, a11y, page inventory).

## Connecting to the backend (later)

The API gateway runs on `:8080`. Add a Vite dev proxy and a typed API client, then
replace the mock imports in `src/data/content.js` and the Contact form submit with
real calls (`/api/v1/...`). Responses are wrapped in `ApiResponse<T>`.
