import { Card } from "@/components/ui/card";
import { LoadingState } from "./LoadingState";
import { ErrorState } from "./ErrorState";
import { EmptyState } from "./EmptyState";
import { cn } from "@/lib/utils";

/**
 * Generic table with built-in loading / error / empty states.
 *
 * columns: [{ key, header, render?(row), className?, align? }]
 * rows: array of objects
 * rowKey: field name or fn(row) -> key
 */
export function DataTable({
  columns,
  rows,
  rowKey = "id",
  isLoading,
  error,
  onRetry,
  empty = {},
  onRowClick,
}) {
  if (isLoading) return <LoadingState />;
  if (error) return <ErrorState error={error} onRetry={onRetry} />;
  if (!rows || rows.length === 0)
    return (
      <EmptyState
        title={empty.title || "Nothing here yet"}
        description={empty.description}
        icon={empty.icon}
        action={empty.action}
      />
    );

  const keyOf = (row, i) =>
    typeof rowKey === "function" ? rowKey(row) : row[rowKey] ?? i;

  return (
    <Card className="overflow-hidden">
      <div className="overflow-x-auto">
        <table className="w-full text-sm">
          <thead>
            <tr className="border-b border-border bg-secondary/50 text-left">
              {columns.map((c) => (
                <th
                  key={c.key}
                  className={cn(
                    "px-5 py-3.5 font-semibold text-navy-700",
                    c.align === "right" && "text-right",
                    c.className
                  )}
                >
                  {c.header}
                </th>
              ))}
            </tr>
          </thead>
          <tbody>
            {rows.map((row, i) => (
              <tr
                key={keyOf(row, i)}
                onClick={onRowClick ? () => onRowClick(row) : undefined}
                className={cn(
                  "border-b border-border/70 last:border-0 transition-colors",
                  onRowClick && "cursor-pointer hover:bg-secondary/40"
                )}
              >
                {columns.map((c) => (
                  <td
                    key={c.key}
                    className={cn(
                      "px-5 py-4 text-navy-800",
                      c.align === "right" && "text-right",
                      c.cellClassName
                    )}
                  >
                    {c.render ? c.render(row) : row[c.key]}
                  </td>
                ))}
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </Card>
  );
}
