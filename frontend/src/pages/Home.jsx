import { Link } from "react-router-dom";
import { ArrowRight, CheckCircle2, Quote, Star } from "lucide-react";
import { Button } from "@/components/ui/button";
import { Card, CardContent } from "@/components/ui/card";
import { Badge } from "@/components/ui/badge";
import { SectionHeading } from "@/components/layout/SectionHeading";
import {
  stats,
  features,
  steps,
  categories,
  testimonials,
  trustBadges,
} from "@/data/content";

export default function Home() {
  return (
    <>
      {/* ---------- Hero ---------- */}
      <section className="relative overflow-hidden bg-grid">
        <div className="absolute inset-0 -z-10 bg-gradient-to-b from-secondary/60 to-background" />
        <div className="container grid items-center gap-12 py-20 lg:grid-cols-2 lg:py-28">
          <div className="animate-fade-up">
            <Badge variant="accent">Trade beyond borders</Badge>
            <h1 className="mt-5 text-4xl font-extrabold leading-[1.1] text-balance sm:text-5xl lg:text-6xl">
              The trusted B2B marketplace for{" "}
              <span className="text-primary">global export & import</span>
            </h1>
            <p className="mt-6 max-w-xl text-lg leading-relaxed text-muted-foreground">
              Hirkani Exim connects verified buyers and sellers worldwide — source
              by HS code, negotiate quotes, and settle securely with escrow-backed
              payments. All in one platform.
            </p>
            <div className="mt-8 flex flex-wrap gap-3">
              <Button variant="accent" size="lg">
                Get started free <ArrowRight />
              </Button>
              <Button variant="outline" size="lg" asChild>
                <Link to="/how-it-works">See how it works</Link>
              </Button>
            </div>
            <div className="mt-8 flex flex-wrap gap-x-6 gap-y-3">
              {trustBadges.map((b) => (
                <span
                  key={b.label}
                  className="inline-flex items-center gap-2 text-sm font-medium text-navy-700"
                >
                  <b.icon className="size-4 text-teal-600" /> {b.label}
                </span>
              ))}
            </div>
          </div>

          {/* Hero visual — mock deal card */}
          <div className="animate-fade-up [animation-delay:120ms]">
            <Card className="rotate-1 shadow-card transition-transform hover:rotate-0">
              <CardContent className="p-6">
                <div className="flex items-center justify-between">
                  <Badge variant="teal">Order #EX-20841</Badge>
                  <span className="text-xs font-medium text-muted-foreground">
                    Escrow funded
                  </span>
                </div>
                <div className="mt-5 space-y-4">
                  <Row label="Product" value="Basmati Rice — HS 1006.30" />
                  <Row label="Buyer" value="Nordia Trading AB · 🇸🇪" />
                  <Row label="Seller" value="Sahyadri Agro · 🇮🇳" />
                  <Row label="Quantity" value="24 MT" />
                  <div className="flex items-center justify-between border-t border-border pt-4">
                    <span className="text-sm text-muted-foreground">Order value</span>
                    <span className="font-display text-2xl font-bold text-navy-900">
                      $38,400
                    </span>
                  </div>
                </div>
                <div className="mt-5 grid grid-cols-4 gap-1.5">
                  {["Quote", "Order", "Shipped", "Paid"].map((s, i) => (
                    <div key={s} className="text-center">
                      <div
                        className={`h-1.5 rounded-full ${i <= 2 ? "bg-teal-500" : "bg-border"}`}
                      />
                      <span className="mt-1.5 block text-[10px] font-medium text-muted-foreground">
                        {s}
                      </span>
                    </div>
                  ))}
                </div>
              </CardContent>
            </Card>
          </div>
        </div>
      </section>

      {/* ---------- Stats ---------- */}
      <section className="border-y border-border bg-navy-900">
        <div className="container grid grid-cols-2 gap-8 py-12 lg:grid-cols-4">
          {stats.map((s) => (
            <div key={s.label} className="text-center">
              <div className="font-display text-3xl font-bold text-white sm:text-4xl">
                {s.value}
              </div>
              <div className="mt-1 text-sm text-navy-100/70">{s.label}</div>
            </div>
          ))}
        </div>
      </section>

      {/* ---------- Features ---------- */}
      <section className="py-20 lg:py-28">
        <div className="container">
          <SectionHeading
            eyebrow="Why Hirkani Exim"
            title="Everything you need to trade across borders"
            subtitle="From verified counterparties to escrow-protected settlement — the full export-import workflow in one platform."
          />
          <div className="mt-14 grid gap-6 sm:grid-cols-2 lg:grid-cols-3">
            {features.map((f) => (
              <Card key={f.title} className="group transition-shadow hover:shadow-card">
                <CardContent className="p-7">
                  <div className="grid size-12 place-items-center rounded-xl bg-secondary text-primary transition-colors group-hover:bg-primary group-hover:text-white">
                    <f.icon className="size-6" />
                  </div>
                  <h3 className="mt-5 text-lg font-semibold">{f.title}</h3>
                  <p className="mt-2 leading-relaxed text-muted-foreground">{f.body}</p>
                </CardContent>
              </Card>
            ))}
          </div>
        </div>
      </section>

      {/* ---------- How it works ---------- */}
      <section className="bg-secondary/50 py-20 lg:py-28">
        <div className="container">
          <SectionHeading
            eyebrow="How it works"
            title="Four steps from sourcing to settlement"
          />
          <div className="mt-14 grid gap-8 md:grid-cols-2 lg:grid-cols-4">
            {steps.map((s, i) => (
              <div key={s.title} className="relative">
                <div className="grid size-14 place-items-center rounded-2xl bg-primary text-white shadow-soft">
                  <s.icon className="size-6" />
                </div>
                <span className="absolute right-2 top-0 font-display text-5xl font-bold text-navy-900/5">
                  0{i + 1}
                </span>
                <h3 className="mt-5 text-lg font-semibold">{s.title}</h3>
                <p className="mt-2 leading-relaxed text-muted-foreground">{s.body}</p>
              </div>
            ))}
          </div>
          <div className="mt-12 text-center">
            <Button variant="default" asChild>
              <Link to="/how-it-works">
                Explore the full workflow <ArrowRight />
              </Link>
            </Button>
          </div>
        </div>
      </section>

      {/* ---------- Categories ---------- */}
      <section className="py-20 lg:py-28">
        <div className="container">
          <SectionHeading
            eyebrow="Product categories"
            title="Trade across every major HS chapter"
            subtitle="Thousands of products, organized by Harmonized System code for precise sourcing."
          />
          <div className="mt-14 grid gap-4 sm:grid-cols-2 lg:grid-cols-3">
            {categories.map((c) => (
              <Card
                key={c.name}
                className="flex items-center justify-between p-5 transition-all hover:-translate-y-0.5 hover:shadow-card"
              >
                <div>
                  <h3 className="text-base font-semibold">{c.name}</h3>
                  <p className="mt-1 text-sm text-muted-foreground">{c.count}</p>
                </div>
                <Badge variant="outline">{c.hs}</Badge>
              </Card>
            ))}
          </div>
        </div>
      </section>

      {/* ---------- Testimonials ---------- */}
      <section className="bg-secondary/50 py-20 lg:py-28">
        <div className="container">
          <SectionHeading
            eyebrow="Trusted worldwide"
            title="Exporters and importers rely on us"
          />
          <div className="mt-14 grid gap-6 lg:grid-cols-3">
            {testimonials.map((t) => (
              <Card key={t.name} className="flex flex-col p-7">
                <Quote className="size-8 text-gold-500" />
                <div className="mt-3 flex gap-0.5">
                  {Array.from({ length: 5 }).map((_, i) => (
                    <Star key={i} className="size-4 fill-gold-400 text-gold-400" />
                  ))}
                </div>
                <p className="mt-4 flex-1 leading-relaxed text-navy-800">"{t.quote}"</p>
                <div className="mt-6">
                  <div className="font-semibold text-navy-900">{t.name}</div>
                  <div className="text-sm text-muted-foreground">{t.role}</div>
                </div>
              </Card>
            ))}
          </div>
        </div>
      </section>

      {/* ---------- CTA ---------- */}
      <CtaBand />
    </>
  );
}

function Row({ label, value }) {
  return (
    <div className="flex items-center justify-between text-sm">
      <span className="text-muted-foreground">{label}</span>
      <span className="font-medium text-navy-900">{value}</span>
    </div>
  );
}

export function CtaBand() {
  return (
    <section className="py-20 lg:py-24">
      <div className="container">
        <div className="relative overflow-hidden rounded-3xl bg-primary px-8 py-16 text-center shadow-card sm:px-16">
          <div className="absolute inset-0 -z-0 bg-grid opacity-30" />
          <div className="relative">
            <h2 className="mx-auto max-w-2xl text-3xl font-bold text-white text-balance sm:text-4xl">
              Ready to trade with confidence?
            </h2>
            <p className="mx-auto mt-4 max-w-xl text-lg text-navy-100/80">
              Join thousands of verified companies trading securely on Hirkani Exim.
              Set up takes minutes.
            </p>
            <div className="mt-8 flex flex-wrap justify-center gap-3">
              <Button variant="accent" size="lg">
                Create your account <ArrowRight />
              </Button>
              <Button
                size="lg"
                className="border border-white/30 bg-transparent text-white hover:bg-white/10"
                asChild
              >
                <Link to="/contact">Book a demo</Link>
              </Button>
            </div>
            <div className="mt-6 flex flex-wrap justify-center gap-x-6 gap-y-2 text-sm text-navy-100/70">
              <span className="inline-flex items-center gap-1.5">
                <CheckCircle2 className="size-4 text-gold-500" /> No setup fees
              </span>
              <span className="inline-flex items-center gap-1.5">
                <CheckCircle2 className="size-4 text-gold-500" /> Free Starter plan
              </span>
              <span className="inline-flex items-center gap-1.5">
                <CheckCircle2 className="size-4 text-gold-500" /> Cancel anytime
              </span>
            </div>
          </div>
        </div>
      </div>
    </section>
  );
}
