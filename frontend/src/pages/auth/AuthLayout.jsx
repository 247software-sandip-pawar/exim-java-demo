import { Link } from "react-router-dom";
import { ShieldCheck, Globe2, Wallet } from "lucide-react";
import { Logo } from "@/components/layout/Logo";

export function AuthLayout({ title, subtitle, children, footer }) {
  return (
    <div className="grid min-h-screen lg:grid-cols-2">
      {/* Brand panel */}
      <div className="relative hidden flex-col justify-between bg-navy-900 p-12 text-white lg:flex">
        <div className="absolute inset-0 bg-grid opacity-20" />
        <div className="relative">
          <Link to="/">
            <Logo variant="light" />
          </Link>
        </div>
        <div className="relative">
          <h2 className="max-w-md font-display text-3xl font-bold leading-tight">
            The trusted B2B marketplace for global export & import.
          </h2>
          <ul className="mt-8 space-y-4 text-navy-100/80">
            <li className="flex items-center gap-3">
              <ShieldCheck className="size-5 text-gold-500" /> Verified counterparties only
            </li>
            <li className="flex items-center gap-3">
              <Wallet className="size-5 text-gold-500" /> Escrow-protected payments
            </li>
            <li className="flex items-center gap-3">
              <Globe2 className="size-5 text-gold-500" /> 120+ countries, one platform
            </li>
          </ul>
        </div>
        <p className="relative text-sm text-navy-100/50">
          © 2026 Hirkani Exim
        </p>
      </div>

      {/* Form panel */}
      <div className="flex flex-col justify-center px-6 py-12 sm:px-12">
        <div className="mx-auto w-full max-w-md">
          <div className="lg:hidden">
            <Link to="/">
              <Logo />
            </Link>
          </div>
          <h1 className="mt-8 text-2xl font-bold text-navy-900 lg:mt-0">{title}</h1>
          {subtitle && <p className="mt-2 text-muted-foreground">{subtitle}</p>}
          <div className="mt-8">{children}</div>
          {footer && <div className="mt-6 text-sm text-muted-foreground">{footer}</div>}
        </div>
      </div>
    </div>
  );
}
