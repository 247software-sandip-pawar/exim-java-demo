import { api } from "@/lib/api";

/** messaging-service endpoints (via gateway :8086). No WebSocket yet — poll on refetch. */

export const listConversations = (params) => api.get("/conversations", { params });
export const getConversation = (id) => api.get(`/conversations/${id}`);
export const createConversation = (body) => api.post("/conversations", body);
export const listMessages = (id, params) =>
  api.get(`/conversations/${id}/messages`, { params });
export const postMessage = (id, body) =>
  api.post(`/conversations/${id}/messages`, body);
