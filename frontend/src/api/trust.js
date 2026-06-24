import { api } from "@/lib/api";

/** trust-service endpoints (via gateway :8092). */

// Ratings
export const listRatings = (params) => api.get("/ratings", { params });
export const createRating = (body) => api.post("/ratings", body);
export const getRatingSummary = (ratedCompanyId) =>
  api.get("/ratings/summary", { params: { ratedCompanyId } });

// Disputes
export const listDisputes = (params) => api.get("/disputes", { params });
export const getDispute = (id) => api.get(`/disputes/${id}`);
export const createDispute = (body) => api.post("/disputes", body);
export const updateDisputeStatus = (id, body) =>
  api.patch(`/disputes/${id}/status`, body); // { status, resolution }
