import { api } from "@/lib/api";

/** sourcing-service endpoints (via gateway :8084). */

export const listRfqs = (params) => api.get("/rfqs", { params });
export const getRfq = (id) => api.get(`/rfqs/${id}`);
export const createRfq = (body) => api.post("/rfqs", body);
export const getRfqMatches = (id) => api.get(`/rfqs/${id}/matches`);
