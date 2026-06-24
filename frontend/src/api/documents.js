import { api } from "@/lib/api";

/** documents-service endpoints (via gateway :8088). */

export const listDocuments = (params) => api.get("/documents", { params });
export const getDocument = (id) => api.get(`/documents/${id}`);
// Generate a trade document for an order: { orderId, type }.
export const generateDocument = (body) => api.post("/documents", body);
