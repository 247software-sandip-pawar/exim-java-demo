import { api } from "@/lib/api";

/** catalog-service endpoints (via gateway :8083). */

// HS codes (reference)
export const listHsCodes = (params) => api.get("/hs-codes", { params });
export const getHsCode = (code) => api.get(`/hs-codes/${code}`);

// Products
export const listProducts = (params) => api.get("/products", { params });
export const getProduct = (id) => api.get(`/products/${id}`);
export const createProduct = (body) => api.post("/products", body);
export const updateProduct = (id, body) => api.put(`/products/${id}`, body);
export const deleteProduct = (id) => api.delete(`/products/${id}`);
