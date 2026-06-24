import {
  LayoutDashboard,
  Package,
  Hash,
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
  BadgeCheck,
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
      { to: "/app/catalog", label: "Catalog", icon: Package },
      { to: "/app/hs-codes", label: "HS Codes", icon: Hash },
      { to: "/app/sourcing", label: "Sourcing / RFQs", icon: Search },
      { to: "/app/quotes", label: "Quotes", icon: ScrollText },
      { to: "/app/orders", label: "Orders", icon: ShoppingCart },
      { to: "/app/messages", label: "Messages", icon: MessagesSquare },
      { to: "/app/documents", label: "Documents", icon: FileText },
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
      { to: "/app/verification", label: "Verification", icon: BadgeCheck },
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
