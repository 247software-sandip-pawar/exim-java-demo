import { Link } from "react-router-dom";
import {
  Package,
  ScrollText,
  ShoppingCart,
  Wallet,
  ArrowRight,
  BadgeCheck,
} from "lucide-react";
import { useAuth } from "@/auth/AuthContext";
import { PageHeader } from "@/components/common/PageHeader";
import { Card, CardContent } from "@/components/ui/card";
import { Badge } from "@/components/ui/badge";

const quickStats = [
  { label: "Active products", value: "—", icon: Package, to: "/app/catalog" },
  { label: "Open quotes", value: "—", icon: ScrollText, to: "/app/quotes" },
  { label: "Orders", value: "—", icon: ShoppingCart, to: "/app/orders" },
  { label: "In escrow", value: "—", icon: Wallet, to: "/app/payments" },
];

export default function DashboardHome() {
  const { user } = useAuth();
  const firstName = user?.name?.split(" ")[0] || "there";

  return (
    <div className="space-y-8">
      <PageHeader
        title={`Welcome back, ${firstName}`}
        description="Here's your trade activity at a glance."
      />

      <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-4">
        {quickStats.map((s) => (
          <Link key={s.label} to={s.to}>
            <Card className="transition-all hover:-translate-y-0.5 hover:shadow-card">
              <CardContent className="flex items-center justify-between p-5">
                <div>
                  <p className="text-sm text-muted-foreground">{s.label}</p>
                  <p className="mt-1 font-display text-2xl font-bold text-navy-900">
                    {s.value}
                  </p>
                </div>
                <span className="grid size-11 place-items-center rounded-xl bg-secondary text-primary">
                  <s.icon className="size-5" />
                </span>
              </CardContent>
            </Card>
          </Link>
        ))}
      </div>

      <Card>
        <CardContent className="flex flex-col items-start gap-4 p-6 sm:flex-row sm:items-center sm:justify-between">
          <div className="flex items-start gap-4">
            <span className="grid size-11 place-items-center rounded-xl bg-gold-400/20 text-gold-600">
              <BadgeCheck className="size-6" />
            </span>
            <div>
              <h3 className="font-semibold text-navy-900">Complete your verification</h3>
              <p className="mt-1 text-sm text-muted-foreground">
                Submit your KYC documents to unlock trading on the platform.
              </p>
            </div>
          </div>
          <Link
            to="/app/company"
            className="inline-flex items-center gap-2 rounded-lg bg-primary px-4 py-2 text-sm font-semibold text-white hover:bg-navy-800"
          >
            Start verification <ArrowRight className="size-4" />
          </Link>
        </CardContent>
      </Card>

      <Card className="border-dashed">
        <CardContent className="p-6">
          <div className="flex items-center gap-2">
            <Badge variant="outline">Foundation ready</Badge>
            <span className="text-sm text-muted-foreground">
              Auth, app shell, and the API client are wired. Trade modules ship in the
              next phases.
            </span>
          </div>
        </CardContent>
      </Card>
    </div>
  );
}
