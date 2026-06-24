// Enum values mirrored from the backend (identity-service).
export const ROLES = ["COMPANY_ADMIN", "COMPANY_MEMBER", "PLATFORM_ADMIN", "SUPPORT"];

export const COMPANY_TYPES = [
  "EXPORTER",
  "IMPORTER",
  "BOTH",
  "CHA",
  "FREIGHT_FORWARDER",
];

export const labelize = (v) =>
  v ? String(v).replace(/_/g, " ").replace(/\b\w/g, (c) => c.toUpperCase()) : "";
