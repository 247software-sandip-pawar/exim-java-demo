import { Spinner } from "@/components/ui/spinner";
import { cn } from "@/lib/utils";

export function LoadingState({ label = "Loading…", className }) {
  return (
    <div
      className={cn(
        "flex flex-col items-center justify-center gap-3 py-16 text-muted-foreground",
        className
      )}
    >
      <Spinner className="size-7 text-primary" />
      <span className="text-sm">{label}</span>
    </div>
  );
}
