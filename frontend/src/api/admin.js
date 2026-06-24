import { api } from "@/lib/api";

/** admin-service endpoints (via gateway :8094). PLATFORM_ADMIN only. */

// KYC decisions (applied to verification-service over REST, audit-logged)
export const approveKyc = (id, note) =>
  api.post(`/admin/verifications/${id}/approve`, note ? { note } : {});
export const rejectKyc = (id, note) =>
  api.post(`/admin/verifications/${id}/reject`, note ? { note } : {});

// Sanctions screening
export const screenSanctions = (name) => api.post("/admin/sanctions/screen", { name });
export const listSanctions = (params) => api.get("/admin/sanctions", { params });

// Audit log + metrics
export const listAdminActions = (params) => api.get("/admin/actions", { params });
export const getMetrics = () => api.get("/admin/metrics");
