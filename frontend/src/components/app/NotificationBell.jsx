import { Bell, CheckCheck } from "lucide-react";
import { useQueryClient } from "@tanstack/react-query";
import {
  DropdownMenu,
  DropdownLabel,
  DropdownSeparator,
} from "@/components/ui/dropdown-menu";
import { useAuth } from "@/auth/AuthContext";
import { useApiQuery, useApiMutation } from "@/hooks/useApi";
import { listNotifications, markNotificationRead } from "@/api/notification";
import { formatDateTime } from "@/lib/format";
import { cn } from "@/lib/utils";

/** Topbar notification bell — in-app notifications (GET /notifications), poll-refreshed. */
export function NotificationBell() {
  const { user } = useAuth();
  const qc = useQueryClient();
  const companyId = user?.companyId;

  const query = useApiQuery(
    ["notifications", companyId],
    () => listNotifications({ recipientCompanyId: companyId, size: 10 }),
    { enabled: Boolean(companyId), refetchInterval: 20000 }
  );

  const items = query.data?.items || [];
  const unread = items.filter((n) => !n.read).length;

  const markRead = useApiMutation((id) => markNotificationRead(id), {
    onSuccess: () => qc.invalidateQueries({ queryKey: ["notifications"] }),
  });

  return (
    <DropdownMenu
      align="right"
      className="min-w-80"
      trigger={
        <span className="relative grid size-10 place-items-center rounded-lg text-navy-700 hover:bg-secondary">
          <Bell className="size-5" />
          {unread > 0 && (
            <span className="absolute right-2 top-2 size-2 rounded-full bg-accent ring-2 ring-card" />
          )}
        </span>
      }
    >
      <DropdownLabel>
        Notifications{unread > 0 ? ` · ${unread} unread` : ""}
      </DropdownLabel>
      <DropdownSeparator />
      {items.length === 0 ? (
        <p className="px-3 py-6 text-center text-sm text-muted-foreground">
          You're all caught up.
        </p>
      ) : (
        <ul className="max-h-96 overflow-y-auto">
          {items.map((n) => (
            <li
              key={n.id}
              className={cn(
                "border-b border-border/60 px-3 py-2.5 last:border-0",
                !n.read && "bg-secondary/40"
              )}
            >
              <div className="flex items-start justify-between gap-2">
                <div className="min-w-0">
                  <p className="truncate text-sm font-medium text-navy-900">{n.title}</p>
                  {n.body && <p className="mt-0.5 text-xs text-muted-foreground">{n.body}</p>}
                  <p className="mt-1 text-[10px] text-muted-foreground">{formatDateTime(n.createdAt)}</p>
                </div>
                {!n.read && (
                  <button
                    title="Mark read"
                    onClick={() => markRead.mutate(n.id)}
                    disabled={markRead.isPending}
                    className="shrink-0 rounded-md p-1 text-muted-foreground hover:bg-secondary hover:text-navy-800"
                  >
                    <CheckCheck className="size-4" />
                  </button>
                )}
              </div>
            </li>
          ))}
        </ul>
      )}
    </DropdownMenu>
  );
}
