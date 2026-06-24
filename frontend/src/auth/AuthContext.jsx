import {
  createContext,
  useContext,
  useEffect,
  useState,
  useCallback,
} from "react";
import {
  api,
  setAuthToken,
  registerUnauthorizedHandler,
} from "@/lib/api";

const TOKEN_KEY = "hx_token";
const AuthContext = createContext(null);

export function AuthProvider({ children }) {
  const [token, setToken] = useState(() => localStorage.getItem(TOKEN_KEY));
  const [user, setUser] = useState(null);
  const [loading, setLoading] = useState(Boolean(token));

  // Keep the api client + storage in sync with the token.
  useEffect(() => {
    setAuthToken(token);
    if (token) localStorage.setItem(TOKEN_KEY, token);
    else localStorage.removeItem(TOKEN_KEY);
  }, [token]);

  const logout = useCallback(() => {
    setToken(null);
    setUser(null);
  }, []);

  // On 401/403 (expired/invalid JWT) drop the session.
  useEffect(() => {
    registerUnauthorizedHandler(() => logout());
  }, [logout]);

  // Hydrate the current user when we have a token but no user yet.
  useEffect(() => {
    let active = true;
    if (token && !user) {
      api
        .get("/users/me")
        .then((me) => active && setUser(me))
        .catch(() => active && logout())
        .finally(() => active && setLoading(false));
    } else {
      setLoading(false);
    }
    return () => {
      active = false;
    };
  }, [token, user, logout]);

  const login = useCallback(async (email, password) => {
    const data = await api.post("/auth/login", { email, password });
    setToken(data.accessToken);
    setUser(data.user);
    return data.user;
  }, []);

  const register = useCallback(async (payload) => {
    // register returns the same AuthResponse { accessToken, user } as login.
    const data = await api.post("/auth/register", payload);
    setToken(data.accessToken);
    setUser(data.user);
    return data.user;
  }, []);

  const hasRole = useCallback(
    (...roles) => (user ? roles.includes(user.role) : false),
    [user]
  );

  const value = {
    user,
    token,
    loading,
    isAuthenticated: Boolean(token),
    login,
    register,
    logout,
    hasRole,
  };

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

export function useAuth() {
  const ctx = useContext(AuthContext);
  if (!ctx) throw new Error("useAuth must be used within <AuthProvider>");
  return ctx;
}
