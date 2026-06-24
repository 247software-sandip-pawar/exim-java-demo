import { Check, Sparkles } from "lucide-react";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardHeader } from "@/components/ui/card";
import { Badge } from "@/components/ui/badge";
import { Accordion } from "@/components/ui/accordion";
import { PageHero } from "@/components/layout/PageHero";
import { SectionHeading } from "@/components/layout/SectionHeading";
import { plans } from "@/data/content";
import { cn } from "@/lib/utils";

const faqs = [
  {
    q: "Is there really a free plan?",
    a: "Yes. The Starter plan is free forever and lets you create a verified profile, list products, and trade with escrow at standard rates. Upgrade when you need more volume.",
  },
  {
    q: "What is the escrow fee?",
    a: "Escrow fees range from 2.5% on Starter down to 1.5% on Growth, with custom rates for Enterprise. The fee covers escrow handling and dispute protection.",
  },
  {
    q: "Can I change plans later?",
    a: "Absolutely. Upgrade or downgrade anytime — only one active subscription per company, and changes prorate automatically.",
  },
];

export default function Pricing() {
  return (
    <>
      <PageHero
        eyebrow="Pricing"
        title="Simple plans that scale with your trade"
        subtitle="Start free and upgrade as your deal flow grows. No setup fees, cancel anytime."
      />

      <section className="py-16 lg:py-20">
        <div className="container grid items-start gap-6 lg:grid-cols-3">
          {plans.map((plan) => (
            <Card
              key={plan.name}
              className={cn(
                "relative flex flex-col",
                plan.highlight &&
                  "border-primary shadow-card ring-1 ring-primary lg:-mt-4 lg:mb-4"
              )}
            >
              {plan.highlight && (
                <div className="absolute -top-3 left-1/2 -translate-x-1/2">
                  <Badge variant="accent" className="shadow-soft">
                    <Sparkles className="size-3.5" /> Most popular
                  </Badge>
                </div>
              )}
              <CardHeader className="p-8 pb-0">
                <h3 className="text-xl font-bold">{plan.name}</h3>
                <p className="text-sm text-muted-foreground">{plan.tagline}</p>
                <div className="mt-4 flex items-end gap-1">
                  <span className="font-display text-4xl font-bold text-navy-900">
                    {plan.price}
                  </span>
                  {plan.period && (
                    <span className="mb-1 text-muted-foreground">{plan.period}</span>
                  )}
                </div>
              </CardHeader>
              <CardContent className="flex flex-1 flex-col p-8 pt-6">
                <Button
                  variant={plan.highlight ? "accent" : "outline"}
                  className="w-full"
                >
                  {plan.cta}
                </Button>
                <ul className="mt-7 space-y-3">
                  {plan.features.map((f) => (
                    <li key={f} className="flex items-start gap-3 text-sm">
                      <Check className="mt-0.5 size-4 shrink-0 text-teal-600" />
                      <span className="text-navy-800">{f}</span>
                    </li>
                  ))}
                </ul>
              </CardContent>
            </Card>
          ))}
        </div>
      </section>

      <section className="pb-20 lg:pb-28">
        <div className="container max-w-3xl">
          <SectionHeading eyebrow="Pricing FAQ" title="Good to know" />
          <Accordion items={faqs} className="mt-12" />
        </div>
      </section>
    </>
  );
}
