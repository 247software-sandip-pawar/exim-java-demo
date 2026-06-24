import { api } from "@/lib/api";

/** billing-service endpoints (via gateway :8091). */

// Plans (seeded reference)
export const listPlans = (params) => api.get("/plans", { params });
export const getPlan = (id) => api.get(`/plans/${id}`);

// Subscriptions
export const listSubscriptions = (params) => api.get("/subscriptions", { params });
export const getSubscription = (id) => api.get(`/subscriptions/${id}`);
export const subscribe = (body) => api.post("/subscriptions", body);
export const cancelSubscription = (id) => api.post(`/subscriptions/${id}/cancel`);

// Platform payments
export const listPlatformPayments = (params) => api.get("/platform-payments", { params });
export const payPlatformPayment = (id) => api.post(`/platform-payments/${id}/pay`);
