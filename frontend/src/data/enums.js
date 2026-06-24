// Enum values mirrored from the backend (identity-service).
export const ROLES = ["COMPANY_ADMIN", "COMPANY_MEMBER", "PLATFORM_ADMIN", "SUPPORT"];

export const COMPANY_TYPES = [
  "EXPORTER",
  "IMPORTER",
  "BOTH",
  "CHA",
  "FREIGHT_FORWARDER",
];

// verification-service
export const VERIFICATION_TYPES = ["IEC", "GST", "RCMC", "BANK"];
export const VERIFICATION_TYPE_LABELS = {
  IEC: "IEC — Import Export Code",
  GST: "GST registration",
  RCMC: "RCMC certificate",
  BANK: "Bank document",
};
export const VERIFICATION_STATUSES = ["PENDING", "APPROVED", "REJECTED"];

export const labelize = (v) =>
  v ? String(v).replace(/_/g, " ").replace(/\b\w/g, (c) => c.toUpperCase()) : "";
