import axios from "axios";

/**
 * Single HTTP client for the EXIM gateway.
 * - Base path /api/v1 (Vite proxies /api -> :8080 in dev).
 * - Request interceptor attaches the JWT.
 * - Response interceptor unwraps ApiResponse<T> -> T and normalizes errors.
 * - 401/403 (expired 15-min JWT) triggers the registered unauthorized handler.
 */

let authToken = null;
let onUnauthorized = null;

export function setAuthToken(token) {
  authToken = token;
}

export function registerUnauthorizedHandler(fn) {
  onUnauthorized = fn;
}

/** Normalized error thrown to callers / React Query. */
export class ApiError extends Error {
  constructor(message, { code, status } = {}) {
    super(message);
    this.name = "ApiError";
    this.code = code;
    this.status = status;
  }
}

export const api = axios.create({
  baseURL: "/api/v1",
  headers: { "Content-Type": "application/json" },
});

api.interceptors.request.use((config) => {
  if (authToken) {
    config.headers.Authorization = `Bearer ${authToken}`;
  }
  return config;
});

api.interceptors.response.use(
  (response) => {
    // Backend wraps everything in ApiResponse { success, data, error, timestamp }.
    const body = response.data;
    if (body && typeof body === "object" && "success" in body) {
      if (body.success) return body.data;
      throw new ApiError(body.error?.message || "Request failed", {
        code: body.error?.code,
        status: response.status,
      });
    }
    return body;
  },
  (error) => {
    const status = error.response?.status;
    if (status === 401 || status === 403) {
      onUnauthorized?.(status);
    }
    const apiErr = error.response?.data?.error;
    return Promise.reject(
      new ApiError(
        apiErr?.message || error.message || "Network error",
        { code: apiErr?.code, status }
      )
    );
  }
);
