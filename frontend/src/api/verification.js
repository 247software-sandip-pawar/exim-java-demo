import { api } from "@/lib/api";

/** verification-service endpoints (via gateway :8082). */

// Company-scoped (KYC submit + own list)
export const listVerifications = (companyId, params) =>
  api.get(`/companies/${companyId}/verifications`, { params });

export const submitVerification = (companyId, body) =>
  api.post(`/companies/${companyId}/verifications`, body);

export const getVerification = (companyId, id) =>
  api.get(`/companies/${companyId}/verifications/${id}`);

// Flat (admin uses these in Phase 6)
// Platform-wide review queue (PLATFORM_ADMIN / SUPPORT); optional ?status=PENDING.
export const listAllVerifications = (params) => api.get("/verifications", { params });
export const getVerificationFlat = (id) => api.get(`/verifications/${id}`);

export const decideVerification = (id, body) =>
  api.patch(`/verifications/${id}/decision`, body); // { status, reviewerNote }
