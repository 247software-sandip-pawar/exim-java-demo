import { Accordion } from "@/components/ui/accordion";
import { Card, CardContent } from "@/components/ui/card";
import { Badge } from "@/components/ui/badge";
import { PageHero } from "@/components/layout/PageHero";
import { SectionHeading } from "@/components/layout/SectionHeading";
import { CtaBand } from "./Home";
import { steps, features } from "@/data/content";

const faqs = [
  {
    q: "How are companies verified?",
    a: "Every company completes KYC — business registration, authorized contact, and document checks — and is screened against a sanctions list before they can trade. Platform admins approve or reject each application.",
  },
  {
    q: "How does escrow protect my payment?",
    a: "Buyers fund escrow once an order is created. Funds are only released to the seller when agreed milestones (such as shipment or delivery) are met. If a dispute arises, funds stay protected until it is resolved.",
  },
  {
    q: "What is HS-code sourcing?",
    a: "Products are organized by Harmonized System (HS) codes — the global standard for classifying traded goods. You can search the catalog by HS code or post a sourcing request and receive quotes from matching sellers.",
  },
  {
    q: "Which trade documents are generated?",
    a: "Once an order is accepted, the platform can generate commercial invoices, packing lists, and certificates of origin from your order data — no manual re-keying.",
  },
  {
    q: "Do you support letters of credit?",
    a: "Yes. Alongside escrow, the payments module supports letters of credit and configurable payment terms for higher-value or longer-cycle trades.",
  },
];

export default function HowItWorks() {
  return (
    <>
      <PageHero
        eyebrow="How it works"
        title="From sourcing to settlement, end to end"
        subtitle="Hirkani Exim digitizes the entire export-import deal flow — so you spend less time on paperwork and more time trading."
      />

      {/* Step timeline */}
      <section className="py-20 lg:py-24">
        <div className="container max-w-4xl">
          <ol className="relative space-y-12 before:absolute before:left-7 before:top-2 before:h-full before:w-px before:bg-border">
            {steps.map((s, i) => (
              <li key={s.title} className="relative flex gap-6">
                <div className="relative z-10 grid size-14 shrink-0 place-items-center rounded-2xl bg-primary text-white shadow-soft">
                  <s.icon className="size-6" />
                </div>
                <div className="pt-1">
                  <span className="text-sm font-semibold text-gold-600">
                    Step {i + 1}
                  </span>
                  <h3 className="mt-1 text-xl font-semibold">{s.title}</h3>
                  <p className="mt-2 leading-relaxed text-muted-foreground">{s.body}</p>
                </div>
              </li>
            ))}
          </ol>
        </div>
      </section>

      {/* Capabilities grid */}
      <section className="bg-secondary/50 py-20 lg:py-24">
        <div className="container">
          <SectionHeading
            eyebrow="Platform capabilities"
            title="Built on a microservice trade backbone"
            subtitle="Identity, verification, catalog, quotation, orders, logistics, payments and more — each a dedicated service behind one secure gateway."
          />
          <div className="mt-14 grid gap-6 sm:grid-cols-2 lg:grid-cols-3">
            {features.map((f) => (
              <Card key={f.title}>
                <CardContent className="p-7">
                  <div className="grid size-12 place-items-center rounded-xl bg-white text-primary shadow-soft">
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

      {/* FAQ */}
      <section className="py-20 lg:py-24">
        <div className="container max-w-3xl">
          <SectionHeading eyebrow="FAQ" title="Questions, answered" />
          <Accordion items={faqs} className="mt-12" />
        </div>
      </section>

      <CtaBand />
    </>
  );
}
