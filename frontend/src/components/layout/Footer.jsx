import { Link } from "react-router-dom";
import { Mail, MapPin, Phone } from "lucide-react";
import { Logo } from "./Logo";

const groups = [
  {
    title: "Platform",
    links: [
      { label: "How it works", to: "/how-it-works" },
      { label: "Pricing", to: "/pricing" },
      { label: "Product catalog", to: "/how-it-works" },
      { label: "Escrow & payments", to: "/how-it-works" },
    ],
  },
  {
    title: "Company",
    links: [
      { label: "About us", to: "/about" },
      { label: "Contact", to: "/contact" },
      { label: "Careers", to: "/about" },
      { label: "Trust & safety", to: "/about" },
    ],
  },
  {
    title: "Resources",
    links: [
      { label: "HS code lookup", to: "/how-it-works" },
      { label: "Trade guides", to: "/how-it-works" },
      { label: "API & docs", to: "/how-it-works" },
      { label: "Support", to: "/contact" },
    ],
  },
];

export function Footer() {
  return (
    <footer className="bg-navy-900 text-navy-100/80">
      <div className="container grid gap-10 py-14 md:grid-cols-2 lg:grid-cols-5">
        <div className="lg:col-span-2">
          <Logo variant="light" />
          <p className="mt-4 max-w-xs text-sm leading-relaxed text-navy-100/70">
            A trusted B2B export-import marketplace connecting verified buyers and
            sellers across borders — from sourcing to escrow-backed payment.
          </p>
          <ul className="mt-6 space-y-2.5 text-sm">
            <li className="flex items-center gap-2.5">
              <Mail className="size-4 text-gold-500" /> hello@hirkaniexim.com
            </li>
            <li className="flex items-center gap-2.5">
              <Phone className="size-4 text-gold-500" /> +91 20 1234 5678
            </li>
            <li className="flex items-center gap-2.5">
              <MapPin className="size-4 text-gold-500" /> Pune, Maharashtra, India
            </li>
          </ul>
        </div>

        {groups.map((g) => (
          <div key={g.title}>
            <h4 className="font-display text-sm font-semibold text-white">{g.title}</h4>
            <ul className="mt-4 space-y-2.5 text-sm">
              {g.links.map((l) => (
                <li key={l.label}>
                  <Link to={l.to} className="transition-colors hover:text-gold-500">
                    {l.label}
                  </Link>
                </li>
              ))}
            </ul>
          </div>
        ))}
      </div>

      <div className="border-t border-white/10">
        <div className="container flex flex-col items-center justify-between gap-3 py-6 text-xs text-navy-100/60 sm:flex-row">
          <p>© {2026} Hirkani Exim. All rights reserved.</p>
          <div className="flex gap-5">
            <Link to="/" className="hover:text-white">Privacy</Link>
            <Link to="/" className="hover:text-white">Terms</Link>
            <Link to="/" className="hover:text-white">Security</Link>
          </div>
        </div>
      </div>
    </footer>
  );
}
