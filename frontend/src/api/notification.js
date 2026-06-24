import { api } from "@/lib/api";

/** notification-service endpoints (via gateway :8093). In-app only (no push). */

export const listNotifications = (params) => api.get("/notifications", { params });
export const markNotificationRead = (id) => api.post(`/notifications/${id}/read`);
