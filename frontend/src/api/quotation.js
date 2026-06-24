import { api } from "@/lib/api";

/** quotation-service endpoints (via gateway :8085). */

export const listQuotes = (params) => api.get("/quotes", { params });
export const getQuote = (id) => api.get(`/quotes/${id}`);
export const submitQuote = (body) => api.post("/quotes", body);
export const acceptQuote = (id) => api.post(`/quotes/${id}/accept`);
export const rejectQuote = (id) => api.post(`/quotes/${id}/reject`);
export const counterQuote = (id, body) => api.post(`/quotes/${id}/counter`, body);
