import * as React from "react";
import { cn } from "@/lib/utils";

const Input = React.forwardRef(({ className, type = "text", ...props }, ref) => (
  <input
    ref={ref}
    type={type}
    className={cn(
      "h-11 w-full rounded-lg border border-input bg-background px-3.5 text-sm text-navy-900 placeholder:text-muted-foreground focus:border-primary focus:outline-none focus:ring-2 focus:ring-ring/30 disabled:cursor-not-allowed disabled:opacity-50",
      className
    )}
    {...props}
  />
));
Input.displayName = "Input";

const Select = React.forwardRef(({ className, ...props }, ref) => (
  <select
    ref={ref}
    className={cn(
      "h-11 w-full rounded-lg border border-input bg-background px-3 text-sm text-navy-900 focus:border-primary focus:outline-none focus:ring-2 focus:ring-ring/30",
      className
    )}
    {...props}
  />
));
Select.displayName = "Select";

const Textarea = React.forwardRef(({ className, ...props }, ref) => (
  <textarea
    ref={ref}
    className={cn(
      "w-full rounded-lg border border-input bg-background px-3.5 py-2.5 text-sm text-navy-900 placeholder:text-muted-foreground focus:border-primary focus:outline-none focus:ring-2 focus:ring-ring/30",
      className
    )}
    {...props}
  />
));
Textarea.displayName = "Textarea";

export { Input, Select, Textarea };
