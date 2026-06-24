import { useNavigate } from "react-router-dom";
import { Menu, LogOut, User, ChevronDown } from "lucide-react";
import { useAuth } from "@/auth/AuthContext";
import {
  DropdownMenu,
  DropdownItem,
  DropdownLabel,
  DropdownSeparator,
} from "@/components/ui/dropdown-menu";
import { NotificationBell } from "./NotificationBell";

export function Topbar({ onMenuClick }) {
  const { user, logout } = useAuth();
  const navigate = useNavigate();

  const initials = (user?.name || "?")
    .split(" ")
    .map((p) => p[0])
    .slice(0, 2)
    .join("")
    .toUpperCase();

  const onLogout = () => {
    logout();
    navigate("/login", { replace: true });
  };

  return (
    <header className="sticky top-0 z-30 flex h-16 items-center justify-between border-b border-border bg-background/80 px-4 backdrop-blur-md lg:px-6">
      <button
        className="grid size-10 place-items-center rounded-lg text-navy-700 hover:bg-secondary lg:hidden"
        onClick={onMenuClick}
        aria-label="Open menu"
      >
        <Menu className="size-5" />
      </button>

      <div className="hidden text-sm text-muted-foreground lg:block">
        {user?.role && (
          <span>
            Signed in as <span className="font-medium text-navy-800">{user.role.replace(/_/g, " ")}</span>
          </span>
        )}
      </div>

      <div className="flex items-center gap-1">
        <NotificationBell />
        <DropdownMenu
          align="right"
          trigger={
            <span className="flex items-center gap-2 rounded-lg py-1.5 pl-1.5 pr-2.5 hover:bg-secondary">
              <span className="grid size-8 place-items-center rounded-full bg-primary text-xs font-semibold text-white">
                {initials}
              </span>
              <span className="hidden text-left sm:block">
                <span className="block text-sm font-medium leading-tight text-navy-900">
                  {user?.name || "Account"}
                </span>
                <span className="block text-xs leading-tight text-muted-foreground">
                  {user?.email}
                </span>
              </span>
              <ChevronDown className="size-4 text-muted-foreground" />
            </span>
          }
        >
          <DropdownLabel>{user?.email}</DropdownLabel>
          <DropdownSeparator />
          <DropdownItem onClick={() => navigate("/app/company")}>
            <User /> Profile & company
          </DropdownItem>
          <DropdownSeparator />
          <DropdownItem className="text-destructive hover:bg-destructive/10" onClick={onLogout}>
            <LogOut /> Sign out
          </DropdownItem>
        </DropdownMenu>
      </div>
    </header>
  );
}
