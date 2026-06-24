import { cn } from "@/lib/utils";

export function Logo({ className, variant = "dark" }) {
  const text = variant === "light" ? "text-white" : "text-navy-900";
  return (
    <span className={cn("inline-flex items-center gap-2.5", className)}>
      <span className="grid size-9 place-items-center rounded-lg bg-primary text-gold-500 shadow-soft">
        <svg viewBox="0 0 32 32" className="size-5 fill-current" aria-hidden>
          <path d="M6 24V8h4v6h12V8h4v16h-4v-6H10v6z" />
        </svg>
      </span>
      <span className={cn("font-display text-lg font-bold leading-none", text)}>
        Hirkani<span className="text-gold-500"> Exim</span>
      </span>
    </span>
  );
}
