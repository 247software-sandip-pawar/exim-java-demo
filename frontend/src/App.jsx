import { useEffect } from "react";
import { Routes, Route, Outlet, useLocation } from "react-router-dom";
import { Navbar } from "@/components/layout/Navbar";
import { Footer } from "@/components/layout/Footer";
import { ProtectedRoute } from "@/auth/RouteGuards";
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
