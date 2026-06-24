import { Label } from "@/components/ui/label";
import { cn } from "@/lib/utils";

/**
 * Wraps a control with a label + error message.
 * Use with react-hook-form: <FormField label="Email" error={errors.email?.message}>...</FormField>
 */
export function FormField({ label, error, hint, required, children, className }) {
  return (
    <div className={cn("space-y-1.5", className)}>
      {label && (
        <Label>
          {label}
          {required && <span className="ml-0.5 text-destructive">*</span>}
        </Label>
      )}
      {children}
      {error ? (
        <p className="text-xs font-medium text-destructive">{error}</p>
      ) : hint ? (
        <p className="text-xs text-muted-foreground">{hint}</p>
      ) : null}
    </div>
  );
}
