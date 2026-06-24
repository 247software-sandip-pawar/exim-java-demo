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

// logistics-service
export const TRANSPORT_MODES = ["SEA", "AIR", "ROAD", "RAIL", "MULTIMODAL"];
export const SHIPMENT_STATUSES = ["CREATED", "BOOKED", "IN_TRANSIT", "ARRIVED", "DELIVERED", "CANCELLED"];
// Allowed next statuses for a tracking event (mirrors ShipmentStatus.allowedNext()).
export const SHIPMENT_STATUS_NEXT = {
  CREATED: ["BOOKED", "CANCELLED"],
  BOOKED: ["IN_TRANSIT", "CANCELLED"],
  IN_TRANSIT: ["ARRIVED", "CANCELLED"],
  ARRIVED: ["DELIVERED"],
  DELIVERED: [],
  CANCELLED: [],
};

// payments-service — escrow transaction lifecycle.
export const PAYMENT_METHODS = ["ESCROW", "LETTER_OF_CREDIT", "WIRE", "NET_TERMS"];
export const TRANSACTION_STATUSES = ["INITIATED", "FUNDED", "RELEASED", "REFUNDED", "FAILED"];
export const TRANSACTION_STATUS_NEXT = {
  INITIATED: ["FUNDED", "FAILED"],
  FUNDED: ["RELEASED", "REFUNDED"],
  RELEASED: [],
  REFUNDED: [],
  FAILED: [],
};

// payments-service — letter of credit lifecycle.
export const LC_STATUSES = ["DRAFT", "ISSUED", "CONFIRMED", "SETTLED", "EXPIRED", "CANCELLED"];
export const LC_STATUS_NEXT = {
  DRAFT: ["ISSUED", "CANCELLED"],
  ISSUED: ["CONFIRMED", "SETTLED", "EXPIRED", "CANCELLED"],
  CONFIRMED: ["SETTLED", "EXPIRED"],
  SETTLED: [],
  EXPIRED: [],
  CANCELLED: [],
};

export const labelize = (v) =>
  v ? String(v).replace(/_/g, " ").replace(/\b\w/g, (c) => c.toUpperCase()) : "";
