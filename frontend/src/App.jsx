import { useEffect } from "react";
import { Routes, Route, Outlet, useLocation } from "react-router-dom";
import { Navbar } from "@/components/layout/Navbar";
import { Footer } from "@/components/layout/Footer";
import { ProtectedRoute, RoleRoute } from "@/auth/RouteGuards";
import { AppShell } from "@/components/app/AppShell";
import { navItems } from "@/config/nav";

import Home from "@/pages/Home";
import About from "@/pages/About";
import HowItWorks from "@/pages/HowItWorks";
import Pricing from "@/pages/Pricing";
import Contact from "@/pages/Contact";
import Login from "@/pages/auth/Login";
import Register from "@/pages/auth/Register";
import DashboardHome from "@/pages/app/DashboardHome";
import ComingSoon from "@/pages/app/ComingSoon";
import Company from "@/pages/app/Company";
import Verification from "@/pages/app/Verification";
import Users from "@/pages/app/admin/Users";
import Companies from "@/pages/app/admin/Companies";
import Catalog from "@/pages/app/catalog/Catalog";
import ProductDetail from "@/pages/app/catalog/ProductDetail";
import HsCodes from "@/pages/app/catalog/HsCodes";
import Rfqs from "@/pages/app/sourcing/Rfqs";
import RfqDetail from "@/pages/app/sourcing/RfqDetail";
import Quotes from "@/pages/app/quotes/Quotes";
import QuoteDetail from "@/pages/app/quotes/QuoteDetail";
import Orders from "@/pages/app/orders/Orders";
import OrderDetail from "@/pages/app/orders/OrderDetail";
import Messages from "@/pages/app/messages/Messages";
import Documents from "@/pages/app/documents/Documents";

function ScrollToTop() {
  const { pathname } = useLocation();
  useEffect(() => window.scrollTo(0, 0), [pathname]);
  return null;
}

/** Public marketing layout (navbar + footer). */
function MarketingLayout() {
  return (
    <div className="flex min-h-screen flex-col">
      <Navbar />
      <main className="flex-1">
        <Outlet />
      </main>
      <Footer />
    </div>
  );
}

// Modules not yet built render a placeholder so the shell nav stays functional.
const soonRoutes = navItems
  .filter((i) => i.soon)
  .map((i) => ({ path: i.to.replace(/^\/app\/?/, ""), label: i.label }));

export default function App() {
  return (
    <>
      <ScrollToTop />
      <Routes>
        {/* Public marketing site */}
        <Route element={<MarketingLayout />}>
          <Route path="/" element={<Home />} />
          <Route path="/about" element={<About />} />
          <Route path="/how-it-works" element={<HowItWorks />} />
          <Route path="/pricing" element={<Pricing />} />
          <Route path="/contact" element={<Contact />} />
        </Route>

        {/* Auth */}
        <Route path="/login" element={<Login />} />
        <Route path="/register" element={<Register />} />

        {/* Authenticated app */}
        <Route element={<ProtectedRoute />}>
          <Route path="/app" element={<AppShell />}>
            <Route index element={<DashboardHome />} />

            {/* Phase 1 — Identity */}
            <Route path="company" element={<Company />} />
            {/* Phase 2 — Verification / KYC */}
            <Route path="verification" element={<Verification />} />

            {/* Phase 3 — Catalog & Sourcing */}
            <Route path="catalog" element={<Catalog />} />
            <Route path="catalog/:productId" element={<ProductDetail />} />
            <Route path="hs-codes" element={<HsCodes />} />
            <Route path="sourcing" element={<Rfqs />} />
            <Route path="sourcing/:rfqId" element={<RfqDetail />} />

            {/* Phase 4 — Deal flow */}
            <Route path="quotes" element={<Quotes />} />
            <Route path="quotes/:quoteId" element={<QuoteDetail />} />
            <Route path="orders" element={<Orders />} />
            <Route path="orders/:orderId" element={<OrderDetail />} />
            <Route path="messages" element={<Messages />} />
            <Route path="documents" element={<Documents />} />
            <Route element={<RoleRoute roles={["PLATFORM_ADMIN", "SUPPORT"]} />}>
              <Route path="users" element={<Users />} />
            </Route>
            <Route element={<RoleRoute roles={["PLATFORM_ADMIN"]} />}>
              <Route path="companies" element={<Companies />} />
            </Route>

            {soonRoutes.map((r) => (
              <Route
                key={r.path}
                path={r.path}
                element={<ComingSoon title={r.label} />}
              />
            ))}
          </Route>
        </Route>
      </Routes>
    </>
  );
}
