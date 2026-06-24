import { Target, Eye, HeartHandshake, ShieldCheck, Globe2, Zap } from "lucide-react";
import { Card, CardContent } from "@/components/ui/card";
import { PageHero } from "@/components/layout/PageHero";
import { SectionHeading } from "@/components/layout/SectionHeading";
import { CtaBand } from "./Home";
import { stats } from "@/data/content";

const values = [
  {
    icon: ShieldCheck,
    title: "Trust first",
    body: "Verification and escrow are not add-ons — they are the foundation of every trade on the platform.",
  },
  {
    icon: Globe2,
    title: "Borderless by design",
    body: "We remove the friction of cross-border trade so a small exporter can reach a global buyer with confidence.",
  },
  {
    icon: Zap,
    title: "Simple over complex",
    body: "Trade is complicated enough. We turn the deal flow into clear, guided steps anyone can follow.",
  },
];

export default function About() {
  return (
    <>
      <PageHero
        eyebrow="About us"
        title="Bringing courage and trust to global trade"
        subtitle="Named after Hirkani — a symbol of determination and resolve — Hirkani Exim exists to make cross-border trade safe, transparent, and within reach for every business."
      />

      {/* Story */}
      <section className="py-20 lg:py-24">
        <div className="container grid items-center gap-12 lg:grid-cols-2">
          <div>
            <h2 className="text-3xl font-bold">Our story</h2>
            <div className="mt-5 space-y-4 leading-relaxed text-muted-foreground">
              <p>
                Export-import has long been gated by brokers, opaque paperwork, and
                the fear of dealing with an unknown counterparty across the world.
                Small and mid-sized businesses paid the highest price.
              </p>
              <p>
                Hirkani Exim was built to change that — a single platform where every
                company is verified, every payment is escrow-protected, and the whole
                journey from sourcing to settlement is transparent and tracked.
              </p>
              <p>
                Today we connect thousands of verified buyers and sellers across more
                than 120 countries, facilitating trade that was once out of reach.
              </p>
            </div>
          </div>
          <div className="grid grid-cols-2 gap-4">
            {stats.map((s) => (
              <Card key={s.label} className="p-6 text-center">
                <div className="font-display text-3xl font-bold text-primary">
                  {s.value}
                </div>
                <div className="mt-1 text-sm text-muted-foreground">{s.label}</div>
              </Card>
            ))}
          </div>
        </div>
      </section>

      {/* Mission / Vision */}
      <section className="bg-secondary/50 py-20 lg:py-24">
        <div className="container grid gap-6 md:grid-cols-2">
          <Card className="p-8">
            <Target className="size-9 text-gold-500" />
            <h3 className="mt-4 text-xl font-semibold">Our mission</h3>
            <p className="mt-2 leading-relaxed text-muted-foreground">
              To make cross-border trade safe and accessible for every business — by
              replacing trust gaps with verification and escrow, and replacing
              paperwork with software.
            </p>
          </Card>
          <Card className="p-8">
            <Eye className="size-9 text-gold-500" />
            <h3 className="mt-4 text-xl font-semibold">Our vision</h3>
            <p className="mt-2 leading-relaxed text-muted-foreground">
              A world where a verified exporter anywhere can trade with a verified
              buyer anywhere — instantly, transparently, and without fear.
            </p>
          </Card>
        </div>
      </section>

      {/* Values */}
      <section className="py-20 lg:py-24">
        <div className="container">
          <SectionHeading
            eyebrow="What we value"
            title="Principles behind the platform"
          />
          <div className="mt-14 grid gap-6 md:grid-cols-3">
            {values.map((v) => (
              <Card key={v.title} className="p-7">
                <CardContent className="p-0">
                  <div className="grid size-12 place-items-center rounded-xl bg-secondary text-primary">
                    <v.icon className="size-6" />
                  </div>
                  <h3 className="mt-5 text-lg font-semibold">{v.title}</h3>
                  <p className="mt-2 leading-relaxed text-muted-foreground">{v.body}</p>
                </CardContent>
              </Card>
            ))}
          </div>
        </div>
      </section>

      <CtaBand />
    </>
  );
}
