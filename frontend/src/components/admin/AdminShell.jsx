import { NavLink, Outlet, useNavigate } from "react-router-dom";
import {
  ShieldCheck,
  LayoutDashboard,
  UserCog,
  Building2,
  Users as UsersIcon,
  Activity as ActivityIcon,
  LogOut,
} from "lucide-react";
import { useAuth } from "@/auth/AuthContext";
import { cn } from "@/lib/utils";

const NAV = [
  { to: "/admin", label: "Console", icon: LayoutDashboard, end: true },
  { to: "/admin/staff", label: "Platform staff", icon: UserCog, adminOnly: true },
  { to: "/admin/companies", label: "Companies", icon: Building2 },
  { to: "/admin/users", label: "Users", icon: UsersIcon },
  { to: "/admin/activity", label: "Activity log", icon: ActivityIcon },
];

/** Dedicated platform-admin portal shell — distinct (dark) layout from the company app. */
export function AdminShell() {
  const { user, hasRole, logout } = useAuth();
  const navigate = useNavigate();
  const items = NAV.filter((i) => !i.adminOnly || hasRole("PLATFORM_ADMIN"));

  const onLogout = () => {
    logout();
    navigate("/admin/login", { replace: true });
  };

  return (
    <div className="min-h-screen bg-navy-950 text-navy-50">
      {/* Sidebar */}
      <aside className="fixed inset-y-0 left-0 hidden w-64 flex-col border-r border-white/10 bg-navy-900 lg:flex">
        <div className="flex h-16 items-center gap-2 border-b border-white/10 px-5">
          <ShieldCheck className="size-5 text-gold-400" />
          <span className="font-semibold text-white">Admin Portal</span>
        </div>
        <nav className="flex-1 space-y-1 p-3">
          {items.map((i) => (
            <NavLink
              key={i.to}
              to={i.to}
              end={i.end}
              className={({ isActive }) =>
                cn(
                  "flex items-center gap-3 rounded-lg px-3 py-2.5 text-sm font-medium transition-colors",
                  isActive
                    ? "bg-white/10 text-white"
                    : "text-navy-200 hover:bg-white/5 hover:text-white"
                )
              }
            >
              <i.icon className="size-4" /> {i.label}
            </NavLink>
          ))}
        </nav>
        <div className="border-t border-white/10 p-3">
          <div className="px-3 py-2 text-xs text-navy-300">
            <p className="truncate font-medium text-navy-100">{user?.name}</p>
            <p className="truncate">{user?.role?.replace(/_/g, " ")}</p>
          </div>
          <button
            onClick={onLogout}
            className="flex w-full items-center gap-3 rounded-lg px-3 py-2.5 text-sm font-medium text-navy-200 hover:bg-white/5 hover:text-white"
          >
            <LogOut className="size-4" /> Sign out
          </button>
        </div>
      </aside>

      {/* Content */}
      <div className="lg:pl-64">
        {/* Mobile top bar */}
        <header className="flex h-16 items-center justify-between border-b border-white/10 px-4 lg:hidden">
          <span className="flex items-center gap-2 font-semibold text-white">
            <ShieldCheck className="size-5 text-gold-400" /> Admin Portal
          </span>
          <button onClick={onLogout} className="text-navy-200 hover:text-white" aria-label="Sign out">
            <LogOut className="size-5" />
          </button>
        </header>
        <main className="min-h-screen bg-secondary/30 text-foreground">
          <div className="mx-auto max-w-6xl px-4 py-6 lg:px-8 lg:py-8">
            <Outlet />
          </div>
        </main>
      </div>
    </div>
  );
}
