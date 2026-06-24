import { api } from "@/lib/api";

/** identity-service endpoints (via gateway :8081). */

// Current user
export const getMe = () => api.get("/users/me");

// Companies
export const getCompany = (id) => api.get(`/companies/${id}`);
export const listCompanies = (params) => api.get("/companies", { params });
export const createCompany = (body) => api.post("/companies", body);
export const updateCompany = (id, body) => api.put(`/companies/${id}`, body);
export const deleteCompany = (id) => api.delete(`/companies/${id}`);
// Moderation: suspend / reinstate a company.
export const setCompanyActive = (id, active) =>
  api.patch(`/companies/${id}/active`, { active });

// Users (admin)
export const listUsers = (params) => api.get("/users", { params });
export const getUser = (id) => api.get(`/users/${id}`);
export const createUser = (body) => api.post("/users", body);
export const updateUser = (id, body) => api.put(`/users/${id}`, body);
export const deleteUser = (id) => api.delete(`/users/${id}`);
// Moderation: deactivate / reactivate a user.
export const setUserActive = (id, active) =>
  api.patch(`/users/${id}/active`, { active });
