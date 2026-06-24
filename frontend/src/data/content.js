import {
  ShieldCheck,
  Search,
  FileText,
  Ship,
  Wallet,
  MessagesSquare,
  BadgeCheck,
  Globe2,
  ScrollText,
} from "lucide-react";

export const stats = [
  { value: "120+", label: "Countries served" },
  { value: "8,400+", label: "Verified companies" },
  { value: "$2.1B", label: "Trade facilitated" },
  { value: "99.2%", label: "Escrow success rate" },
];

export const features = [
  {
    icon: BadgeCheck,
    title: "Verified counterparties",
    body: "Every company passes KYC and sanctions screening before they can trade. Deal only with vetted buyers and sellers.",
  },
  {
    icon: Search,
    title: "HS-code sourcing",
    body: "Search the global catalog by HS code, post sourcing requests, and let qualified sellers come to you.",
  },
  {
    icon: ScrollText,
    title: "Quote-to-order flow",
    body: "Negotiate quotes, accept the best, and convert to a binding order in one click — fully tracked.",
  },
  {
    icon: Wallet,
    title: "Escrow-backed payments",
    body: "Funds are held in escrow and released on agreed milestones. Letters of credit and payment terms built in.",
  },
  {
    icon: Ship,
    title: "Logistics & tracking",
    body: "Book shipments against orders and follow live tracking events through every status milestone.",
  },
  {
    icon: FileText,
    title: "Trade documents",
    body: "Generate invoices, packing lists, and certificates of origin automatically from your order data.",
  },
];

export const steps = [
  {
    icon: BadgeCheck,
    title: "Register & verify",
    body: "Create your company profile and complete KYC verification to unlock trading.",
  },
  {
    icon: Search,
    title: "Source or list",
    body: "Buyers post sourcing requests; sellers list products by HS code and respond with quotes.",
  },
  {
    icon: MessagesSquare,
    title: "Negotiate & order",
    body: "Chat, exchange offers, accept a quote, and convert it into a tracked order.",
  },
  {
    icon: ShieldCheck,
    title: "Pay & ship securely",
    body: "Fund escrow, ship with tracking, release payment on delivery milestones.",
  },
];

export const categories = [
  { name: "Agriculture & Food", hs: "HS 07–24", count: "1,240 products" },
  { name: "Textiles & Apparel", hs: "HS 50–63", count: "980 products" },
  { name: "Chemicals & Pharma", hs: "HS 28–38", count: "760 products" },
  { name: "Machinery & Parts", hs: "HS 84–85", count: "1,510 products" },
  { name: "Metals & Minerals", hs: "HS 72–83", count: "640 products" },
  { name: "Plastics & Rubber", hs: "HS 39–40", count: "520 products" },
];

export const testimonials = [
  {
    quote:
      "We replaced three brokers with Hirkani Exim. Escrow gave our overseas buyers the confidence to pay upfront.",
    name: "Anjali Deshmukh",
    role: "Director, Sahyadri Agro Exports",
  },
  {
    quote:
      "The verification step is what sold us. Every counterparty is KYC-checked, so we stopped chasing fraud.",
    name: "Marcus Lindqvist",
    role: "Procurement Lead, Nordia Trading AB",
  },
  {
    quote:
      "Quote to order to shipment in one dashboard. Our documentation time dropped from days to minutes.",
    name: "Rahul Mehta",
    role: "Founder, Meridian Textiles",
  },
];

export const plans = [
  {
    name: "Starter",
    price: "Free",
    period: "",
    tagline: "For new exporters testing the waters.",
    cta: "Start free",
    highlight: false,
    features: [
      "1 verified company profile",
      "Up to 10 product listings",
      "Browse catalog & post 3 RFQs/mo",
      "In-app messaging",
      "Standard escrow (2.5% fee)",
    ],
  },
  {
    name: "Growth",
    price: "$149",
    period: "/mo",
    tagline: "For active traders scaling deal flow.",
    cta: "Start 14-day trial",
    highlight: true,
    features: [
      "Everything in Starter",
      "Unlimited listings & RFQs",
      "Priority verification (48h)",
      "Letters of credit & payment terms",
      "Logistics booking + live tracking",
      "Reduced escrow fee (1.5%)",
    ],
  },
  {
    name: "Enterprise",
    price: "Custom",
    period: "",
    tagline: "For high-volume exporters & trading houses.",
    cta: "Talk to sales",
    highlight: false,
    features: [
      "Everything in Growth",
      "Dedicated account manager",
      "API access & ERP integration",
      "Custom escrow & LC terms",
      "Multi-team roles & audit logs",
      "SLA-backed support",
    ],
  },
];

export const trustBadges = [
  { icon: ShieldCheck, label: "KYC verified" },
  { icon: Globe2, label: "120+ countries" },
  { icon: Wallet, label: "Escrow protected" },
  { icon: FileText, label: "Trade docs ready" },
];
