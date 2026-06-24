import { useState } from "react";
import { Mail, Phone, MapPin, Send, CheckCircle2 } from "lucide-react";
import { Button } from "@/components/ui/button";
import { Card, CardContent } from "@/components/ui/card";
import { PageHero } from "@/components/layout/PageHero";

const field =
  "w-full rounded-lg border border-input bg-background px-4 py-2.5 text-sm text-navy-900 placeholder:text-muted-foreground focus:border-primary focus:outline-none focus:ring-2 focus:ring-ring/30";

const channels = [
  { icon: Mail, label: "Email", value: "hello@hirkaniexim.com" },
  { icon: Phone, label: "Phone", value: "+91 20 1234 5678" },
  { icon: MapPin, label: "Office", value: "Pune, Maharashtra, India" },
];

export default function Contact() {
  const [sent, setSent] = useState(false);

  return (
    <>
      <PageHero
        eyebrow="Contact"
        title="Let's talk about your trade"
        subtitle="Questions about the platform, a demo, or enterprise pricing? Our team usually replies within one business day."
      />

      <section className="py-16 lg:py-20">
        <div className="container grid gap-10 lg:grid-cols-5">
          {/* Contact info */}
          <div className="lg:col-span-2">
            <h2 className="text-2xl font-bold">Get in touch</h2>
            <p className="mt-3 leading-relaxed text-muted-foreground">
              Reach us directly or send a message and we'll route it to the right team.
            </p>
            <div className="mt-8 space-y-4">
              {channels.map((c) => (
                <div key={c.label} className="flex items-center gap-4">
                  <div className="grid size-11 place-items-center rounded-xl bg-secondary text-primary">
                    <c.icon className="size-5" />
                  </div>
                  <div>
                    <div className="text-sm text-muted-foreground">{c.label}</div>
                    <div className="font-medium text-navy-900">{c.value}</div>
                  </div>
                </div>
              ))}
            </div>
            <Card className="mt-8 bg-secondary/50 p-6">
              <p className="text-sm leading-relaxed text-navy-800">
                <strong>For verified members:</strong> sign in and open a support
                ticket from your dashboard for priority assistance.
              </p>
            </Card>
          </div>

          {/* Form */}
          <div className="lg:col-span-3">
            <Card className="shadow-card">
              <CardContent className="p-8">
                {sent ? (
                  <div className="flex flex-col items-center justify-center py-16 text-center">
                    <CheckCircle2 className="size-14 text-teal-600" />
                    <h3 className="mt-4 text-xl font-semibold">Message sent</h3>
                    <p className="mt-2 max-w-sm text-muted-foreground">
                      Thanks for reaching out. Our team will get back to you within one
                      business day.
                    </p>
                    <Button
                      variant="outline"
                      className="mt-6"
                      onClick={() => setSent(false)}
                    >
                      Send another message
                    </Button>
                  </div>
                ) : (
                  <form
                    onSubmit={(e) => {
                      e.preventDefault();
                      setSent(true);
                    }}
                    className="space-y-5"
                  >
                    <div className="grid gap-5 sm:grid-cols-2">
                      <Label label="Full name">
                        <input className={field} placeholder="Jane Doe" required />
                      </Label>
                      <Label label="Work email">
                        <input
                          type="email"
                          className={field}
                          placeholder="jane@company.com"
                          required
                        />
                      </Label>
                    </div>
                    <div className="grid gap-5 sm:grid-cols-2">
                      <Label label="Company">
                        <input className={field} placeholder="Acme Trading Co." />
                      </Label>
                      <Label label="I am a">
                        <select className={field} defaultValue="">
                          <option value="" disabled>
                            Select…
                          </option>
                          <option>Exporter / Seller</option>
                          <option>Importer / Buyer</option>
                          <option>Logistics partner</option>
                          <option>Other</option>
                        </select>
                      </Label>
                    </div>
                    <Label label="Message">
                      <textarea
                        rows={5}
                        className={field}
                        placeholder="Tell us what you're looking for…"
                        required
                      />
                    </Label>
                    <Button type="submit" variant="accent" size="lg" className="w-full">
                      Send message <Send />
                    </Button>
                  </form>
                )}
              </CardContent>
            </Card>
          </div>
        </div>
      </section>
    </>
  );
}

function Label({ label, children }) {
  return (
    <label className="block">
      <span className="mb-1.5 block text-sm font-medium text-navy-800">{label}</span>
      {children}
    </label>
  );
}
