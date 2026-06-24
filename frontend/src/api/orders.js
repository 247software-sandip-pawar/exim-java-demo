import { api } from "@/lib/api";

/** orders-service endpoints (via gateway :8087). */

export const listOrders = (params) => api.get("/orders", { params });
export const getOrder = (id) => api.get(`/orders/${id}`);
// Create an order from an accepted quote (idempotent on quoteId, backend-side).
export const createOrder = (quoteId) => api.post("/orders", { quoteId });
export const updateOrderStatus = (id, status) =>
  api.patch(`/orders/${id}/status`, { status });
