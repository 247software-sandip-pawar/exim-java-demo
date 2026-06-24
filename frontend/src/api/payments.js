import { api } from "@/lib/api";

/** payments-service endpoints (via gateway :8090). */

// Escrow transactions
export const listPayments = (params) => api.get("/payments", { params });
export const getPayment = (id) => api.get(`/payments/${id}`);
export const initiatePayment = (body) => api.post("/payments", body);
export const fundPayment = (id) => api.post(`/payments/${id}/fund`);
export const releasePayment = (id) => api.post(`/payments/${id}/release`);
export const refundPayment = (id) => api.post(`/payments/${id}/refund`);

// Letters of credit
export const listLettersOfCredit = (params) => api.get("/letters-of-credit", { params });
export const getLetterOfCredit = (id) => api.get(`/letters-of-credit/${id}`);
export const createLetterOfCredit = (body) => api.post("/letters-of-credit", body);
export const updateLcStatus = (id, status) =>
  api.patch(`/letters-of-credit/${id}/status`, { status });

// Payment terms (seeded reference data)
export const listPaymentTerms = (params) => api.get("/payment-terms", { params });
