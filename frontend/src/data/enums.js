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

// quotation-service
export const QUOTE_STATUSES = ["SUBMITTED", "ACCEPTED", "REJECTED", "COUNTERED", "EXPIRED"];

// orders-service — forward-only fulfilment path; CANCELLED allowed from any non-terminal state.
export const ORDER_STATUSES = [
  "CREATED",
  "CONFIRMED",
  "IN_PRODUCTION",
  "SHIPPED",
  "DELIVERED",
  "CANCELLED",
];
// Allowed next statuses (mirrors OrderStatus.allowedNext() on the backend).
export const ORDER_STATUS_NEXT = {
  CREATED: ["CONFIRMED", "CANCELLED"],
  CONFIRMED: ["IN_PRODUCTION", "CANCELLED"],
  IN_PRODUCTION: ["SHIPPED", "CANCELLED"],
  SHIPPED: ["DELIVERED"],
  DELIVERED: [],
  CANCELLED: [],
};

// documents-service
export const DOCUMENT_TYPES = [
  "PROFORMA_INVOICE",
  "COMMERCIAL_INVOICE",
  "PACKING_LIST",
  "BILL_OF_LADING",
  "CERTIFICATE_OF_ORIGIN",
];
export const DOCUMENT_TYPE_LABELS = {
  PROFORMA_INVOICE: "Proforma invoice",
  COMMERCIAL_INVOICE: "Commercial invoice",
  PACKING_LIST: "Packing list",
  BILL_OF_LADING: "Bill of lading",
  CERTIFICATE_OF_ORIGIN: "Certificate of origin",
};

// Standard Incoterms 2020 (used in quotes / in-chat offers).
export const INCOTERMS = ["EXW", "FCA", "FAS", "FOB", "CFR", "CIF", "CPT", "CIP", "DAP", "DPU", "DDP"];

export const labelize = (v) =>
  v ? String(v).replace(/_/g, " ").replace(/\b\w/g, (c) => c.toUpperCase()) : "";
