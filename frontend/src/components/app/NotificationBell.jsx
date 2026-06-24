import { Bell } from "lucide-react";
import {
  DropdownMenu,
  DropdownLabel,
  DropdownSeparator,
} from "@/components/ui/dropdown-menu";

/**
 * Notification bell placeholder. Wires to GET /notifications?unread= in Phase 6;
 * the dropdown + unread dot are ready for that data now.
 */
export function NotificationBell() {
  const unread = 0; // TODO(Phase 6): from useApiQuery(["notifications", { unread: true }])

  return (
    <DropdownMenu
      align="right"
      className="min-w-72"
      trigger={
        <span className="relative grid size-10 place-items-center rounded-lg text-navy-700 hover:bg-secondary">
          <Bell className="size-5" />
          {unread > 0 && (
            <span className="absolute right-2 top-2 size-2 rounded-full bg-accent ring-2 ring-card" />
          )}
        </span>
      }
    >
      <DropdownLabel>Notifications</DropdownLabel>
      <DropdownSeparator />
      <p className="px-3 py-6 text-center text-sm text-muted-foreground">
        You're all caught up.
      </p>
    </DropdownMenu>
  );
}
