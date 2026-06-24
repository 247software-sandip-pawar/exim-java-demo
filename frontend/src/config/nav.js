import {
  LayoutDashboard,
  Package,
  Search,
  FileText,
  ScrollText,
  MessagesSquare,
  ShoppingCart,
  Ship,
  Wallet,
  CreditCard,
  Star,
  AlertOctagon,
  Building2,
  Users,
  ShieldCheck,
} from "lucide-react";

/**
 * Sidebar navigation, grouped to mirror the backend phases.
 * `roles` (optional) restricts visibility. `soon` marks modules not yet built
 * (Phases 1–6) — they render a placeholder until their phase ships.
 */
export const navGroups = [
  {
    label: "Overview",
    items: [{ to: "/app", label: "Dashboard", icon: LayoutDashboard, end: true }],
  },
  {
    label: "Trade",
    items: [
      { to: "/app/catalog", label: "Catalog", icon: Package, soon: true },
      { to: "/app/sourcing", label: "Sourcing / RFQs", icon: Search, soon: true },
      { to: "/app/quotes", label: "Quotes", icon: ScrollText, soon: true },
      { to: "/app/orders", label: "Orders", icon: ShoppingCart, soon: true },
      { to: "/app/messages", label: "Messages", icon: MessagesSquare, soon: true },
      { to: "/app/documents", label: "Documents", icon: FileText, soon: true },
    ],
  },
  {
    label: "Execution",
    items: [
      { to: "/app/shipments", label: "Shipments", icon: Ship, soon: true },
      { to: "/app/payments", label: "Payments", icon: Wallet, soon: true },
    ],
  },
  {
    label: "Business",
    items: [
      { to: "/app/billing", label: "Subscription", icon: CreditCard, soon: true },
      { to: "/app/ratings", label: "Ratings", icon: Star, soon: true },
      { to: "/app/disputes", label: "Disputes", icon: AlertOctagon, soon: true },
    ],
  },
  {
    label: "Account",
    items: [
      { to: "/app/company", label: "Company", icon: Building2 },
      {
        to: "/app/users",
        label: "Users",
        icon: Users,
        roles: ["PLATFORM_ADMIN", "SUPPORT"],
      },
    ],
  },
  {
    label: "Administration",
    roles: ["PLATFORM_ADMIN"],
    items: [
      { to: "/app/companies", label: "Companies", icon: Building2 },
      {
        to: "/app/admin",
        label: "Admin console",
        icon: ShieldCheck,
        roles: ["PLATFORM_ADMIN"],
        soon: true,
      },
    ],
  },
];

/** Flattened list of routable nav items (used to register routes). */
export const navItems = navGroups.flatMap((g) => g.items);
