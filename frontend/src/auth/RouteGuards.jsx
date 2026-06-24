import { Navigate, Outlet, useLocation } from "react-router-dom";
import { useAuth } from "./AuthContext";
import { LoadingState } from "@/components/common/LoadingState";

/** Requires an authenticated session; otherwise sends to /login. */
export function ProtectedRoute() {
  const { isAuthenticated, loading } = useAuth();
  const location = useLocation();

  if (loading) return <LoadingState className="min-h-[60vh]" label="Loading…" />;
  if (!isAuthenticated)
    return <Navigate to="/login" replace state={{ from: location }} />;
  return <Outlet />;
}

/**
 * Guards the admin portal: requires an authenticated platform-staff session
 * (PLATFORM_ADMIN or SUPPORT); otherwise sends to the dedicated admin login.
 */
export function AdminRoute() {
  const { isAuthenticated, loading, hasRole } = useAuth();
  const location = useLocation();

  if (loading) return <LoadingState className="min-h-[60vh]" label="Loading…" />;
  if (!isAuthenticated)
    return <Navigate to="/admin/login" replace state={{ from: location }} />;
  if (!hasRole("PLATFORM_ADMIN", "SUPPORT")) {
    return (
      <div className="container py-24 text-center">
        <h1 className="text-2xl font-bold">Not authorized</h1>
        <p className="mt-2 text-muted-foreground">The admin portal is for platform staff only.</p>
      </div>
    );
  }
  return <Outlet />;
}

/** Requires one of the given roles; otherwise shows a not-authorized notice. */
export function RoleRoute({ roles }) {
  const { hasRole } = useAuth();
  if (!hasRole(...roles)) {
    return (
      <div className="container py-24 text-center">
        <h1 className="text-2xl font-bold">Not authorized</h1>
        <p className="mt-2 text-muted-foreground">
          Your role doesn't have access to this page.
        </p>
      </div>
    );
  }
  return <Outlet />;
}
