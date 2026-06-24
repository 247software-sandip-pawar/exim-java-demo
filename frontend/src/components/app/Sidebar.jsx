import { NavLink } from "react-router-dom";
import { Link } from "react-router-dom";
import { X } from "lucide-react";
import { Logo } from "@/components/layout/Logo";
import { Badge } from "@/components/ui/badge";
import { useAuth } from "@/auth/AuthContext";
import { navGroups } from "@/config/nav";
import { cn } from "@/lib/utils";

export function Sidebar({ open, onClose }) {
  const { hasRole } = useAuth();

  const visibleGroups = navGroups
    .filter((g) => !g.roles || hasRole(...g.roles))
    .map((g) => ({
      ...g,
      items: g.items.filter((i) => !i.roles || hasRole(...i.roles)),
    }))
    .filter((g) => g.items.length > 0);

  return (
    <>
      {open && (
        <div
          className="fixed inset-0 z-40 bg-navy-900/40 lg:hidden"
          onClick={onClose}
        />
      )}
      <aside
        className={cn(
          "fixed inset-y-0 left-0 z-50 flex w-64 flex-col border-r border-border bg-card transition-transform lg:translate-x-0",
          open ? "translate-x-0" : "-translate-x-full"
        )}
      >
        <div className="flex h-16 items-center justify-between border-b border-border px-5">
          <Link to="/app" onClick={onClose}>
            <Logo />
          </Link>
          <button
            className="grid size-9 place-items-center rounded-lg hover:bg-secondary lg:hidden"
            onClick={onClose}
            aria-label="Close menu"
          >
            <X className="size-5" />
          </button>
        </div>

        <nav className="flex-1 space-y-6 overflow-y-auto px-3 py-5">
          {visibleGroups.map((group) => (
            <div key={group.label}>
              <p className="px-3 pb-2 text-xs font-semibold uppercase tracking-wide text-muted-foreground">
                {group.label}
              </p>
              <ul className="space-y-0.5">
                {group.items.map((item) => (
                  <li key={item.to}>
                    <NavLink
                      to={item.to}
                      end={item.end}
                      onClick={onClose}
                      className={({ isActive }) =>
                        cn(
                          "flex items-center gap-3 rounded-lg px-3 py-2 text-sm font-medium text-navy-700 transition-colors hover:bg-secondary",
                          isActive && "bg-primary text-white hover:bg-primary"
                        )
                      }
                    >
                      <item.icon className="size-4 shrink-0" />
                      <span className="flex-1">{item.label}</span>
                      {item.soon && (
                        <Badge variant="outline" className="px-1.5 py-0 text-[10px]">
                          Soon
                        </Badge>
                      )}
                    </NavLink>
                  </li>
                ))}
              </ul>
            </div>
          ))}
        </nav>
      </aside>
    </>
  );
}
