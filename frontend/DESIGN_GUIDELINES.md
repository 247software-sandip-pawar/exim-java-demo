# Hirkani Exim — Design Guidelines

The single source of truth for the look & feel of **hirkaniexim.com**. Follow this for
every new screen so the public site and the future app dashboard stay consistent.

## 1. Brand

- **Name:** Hirkani Exim — "Hirkani" evokes courage, determination, and trust (a fitting
  story for a trust-driven trade platform).
- **Tagline:** _Trade Beyond Borders._
- **Personality:** trustworthy, global, modern, calm. B2B-professional — not flashy.
- **Voice:** clear and confident. Short sentences. Lead with the customer benefit
  ("trade with confidence"), not the technology.

## 2. Color

Defined as HSL CSS variables in `src/index.css` and mapped in `tailwind.config.js`.
Use the semantic Tailwind classes (`bg-primary`, `text-muted-foreground`, …) — never
hard-code hex values in components.

| Token | Hex | Use |
|-------|-----|-----|
| **Navy 700** (primary) | `#0f3f60` | Primary actions, headings, brand mark |
| Navy 900 | `#072131` | Dark sections, footer, hero text |
| **Gold 500** (accent) | `#f4a11e` | Highlight CTAs, badges, emphasis only |
| Teal 600 | `#0f766e` | Success / verified / positive status |
| Secondary | `#eef4f9` | Section backgrounds, chips, hover fills |
| Muted foreground | `~#5b6b78` | Body / secondary text |
| Destructive | `#dc2626` | Errors, destructive actions |

**Rules**
- Navy is the workhorse; gold is a **seasoning** — one gold accent per viewport, not ten.
- Teal means "good/verified/paid." Don't use it decoratively.
- Maintain WCAG AA contrast: navy/teal text on light, white text on navy/primary.

## 3. Typography

- **Display / headings:** `Sora` (weights 600–800) — `font-display`.
- **Body / UI:** `Inter` (400–600) — `font-sans` (default).
- Scale: hero `text-5xl/6xl`, section `text-3xl/4xl`, card title `text-lg`, body
  `text-base`, meta `text-sm`. Use `text-balance` on headings.
- Line length: cap body copy at `max-w-2xl` (~65 chars). Generous `leading-relaxed`.

## 4. Spacing & layout

- **Container:** centered, `max-w-[1280px]`, `px-6`. Use the `.container` class.
- **Section rhythm:** `py-20 lg:py-28` for major sections; alternate white and
  `bg-secondary/50` bands to create rhythm.
- 8px spacing grid (Tailwind's default scale). Don't invent one-off margins.
- **Radius:** `--radius: 0.75rem` → cards `rounded-xl`, buttons/inputs `rounded-lg`,
  pills `rounded-full`.
- **Elevation:** `shadow-soft` (resting), `shadow-card` (hover / featured). Avoid harsh
  shadows; we use soft navy-tinted shadows.

## 5. Components (shadcn/ui pattern)

Primitives live in `src/components/ui/`, composed via `cva` variants + the `cn()` helper.

- **Button** — `default` (navy), `accent` (gold, for the single primary CTA),
  `outline`, `ghost`, `link`. Sizes `sm | default | lg | icon`.
- **Card** — the default surface. Header/Title/Content/Footer subcomponents.
- **Badge** — `default | accent | outline | teal`. For status & eyebrows.
- **Accordion** — FAQ; dependency-free.

To add more shadcn components later: `npx shadcn@latest add <name>` (CSS-var theme is
already wired), or hand-roll following the same `cva` + `cn` pattern.

## 6. Iconography & imagery

- **Icons:** `lucide-react`, `size-4`/`size-5`/`size-6`, stroke style. Consistent set.
- Pair every feature/value with one icon in a rounded `bg-secondary` tile.
- Imagery (when added): real trade context — ports, containers, goods, people —
  desaturated slightly so navy/gold stay dominant. Avoid generic stock clichés.

## 7. Motion

- Subtle and purposeful: `animate-fade-up` for hero entrance, `hover:-translate-y-0.5`
  lift on cards/buttons, 200–300ms transitions. No bouncing, no autoplay carousels.
- Respect `prefers-reduced-motion` for any future heavy animation.

## 8. Accessibility (non-negotiable)

- Semantic HTML (`header`, `nav`, `main`, `footer`, `section`, `ol/ul`).
- All interactive elements keyboard-reachable with visible `focus-visible` rings.
- `aria-label` on icon-only buttons; `aria-expanded` on toggles (see Navbar, Accordion).
- Color is never the only signal (icons + text for status).

## 9. Responsive

- Mobile-first. Breakpoints: `sm 640 · md 768 · lg 1024 · 2xl 1280`.
- Nav collapses to a hamburger < `md`. Grids: 1 col mobile → 2 → 3.
- Tap targets ≥ 44px (`h-11` buttons).

## 10. Page inventory

**Live (this prototype):** Home, How it works, Pricing, About, Contact.

**Next (app, after marketing sign-off)** — reuse the same tokens/components:
Register & KYC · Dashboard · Catalog & product detail · Sourcing/RFQ · Quotations ·
Messaging · Orders · Documents · Logistics tracking · Payments/escrow · Admin.

## 11. Definition of done (per screen)

- [ ] Uses semantic color tokens & the type scale (no magic hex / font sizes)
- [ ] Responsive at 360 / 768 / 1280
- [ ] Keyboard-navigable, visible focus, labeled controls
- [ ] One primary CTA per view (gold), secondary as outline/ghost
- [ ] Empty / loading / error states considered (for app screens)
- [ ] `npm run build` passes
