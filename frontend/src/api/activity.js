import { api } from "@/lib/api";

/** activity-service endpoints (via gateway :8095). Platform-wide audit trail; staff-only reads. */

export const listActivity = (params) => api.get("/activity", { params });
