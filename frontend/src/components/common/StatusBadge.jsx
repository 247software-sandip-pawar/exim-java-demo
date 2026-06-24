import { Badge } from "@/components/ui/badge";

/**
 * Maps the various backend status enums to a badge variant by intent.
 * Falls back to a neutral badge for unknown values.
 */
const POSITIVE = ["APPROVED", "ACTIVE", "ACCEPTED", "PAID", "RELEASED", "FUNDED", "DELIVERED", "RESOLVED", "COMPLETED", "VERIFIED"];
const WARNING = ["PENDING", "SUBMITTED", "IN_REVIEW", "INITIATED", "IN_TRANSIT", "OPEN", "COUNTERED", "PROCESSING"];
const NEGATIVE = ["REJECTED", "CANCELLED", "CANCELED", "REFUNDED", "FAILED", "DISPUTED", "EXPIRED", "CLOSED", "SUSPENDED", "DISABLED"];

export function StatusBadge({ status }) {
  if (!status) return null;
  const s = String(status).toUpperCase();
  let variant = "outline";
  if (POSITIVE.includes(s)) variant = "teal";
  else if (WARNING.includes(s)) variant = "accent";
  else if (NEGATIVE.includes(s)) variant = "default";

  return <Badge variant={variant}>{String(status).replace(/_/g, " ")}</Badge>;
}
