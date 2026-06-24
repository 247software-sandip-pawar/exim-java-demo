import { api } from "@/lib/api";

/** logistics-service endpoints (via gateway :8089). */

// Logistics partners (seeded reference data)
export const listPartners = (params) => api.get("/logistics-partners", { params });

// Shipments
export const listShipments = (params) => api.get("/shipments", { params });
export const getShipment = (id) => api.get(`/shipments/${id}`);
export const createShipment = (body) => api.post("/shipments", body);
export const addTrackingEvent = (id, body) => api.post(`/shipments/${id}/events`, body);
