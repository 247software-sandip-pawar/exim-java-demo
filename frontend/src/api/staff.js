import { api } from "@/lib/api";

/** Platform-staff management (identity-service /staff, via gateway). PLATFORM_ADMIN only. */

export const listStaff = (params) => api.get("/staff", { params });
export const createStaff = (body) => api.post("/staff", body);
export const setStaffActive = (id, active) =>
  api.patch(`/staff/${id}/active`, { active });
