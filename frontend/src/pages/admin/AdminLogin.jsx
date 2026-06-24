import { useState } from "react";
import { useNavigate, useLocation } from "react-router-dom";
import { useForm } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import { z } from "zod";
import { ShieldCheck, ArrowRight } from "lucide-react";
import { useAuth } from "@/auth/AuthContext";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { FormField } from "@/components/common/FormField";

const schema = z.object({
  email: z.string().email("Enter a valid email"),
  password: z.string().min(1, "Password is required"),
});

/** Dedicated platform-staff sign-in, separate from the company app's /login. */
export default function AdminLogin() {
  const { adminLogin } = useAuth();
  const navigate = useNavigate();
  const location = useLocation();
  const [serverError, setServerError] = useState(null);
  const from = location.state?.from?.pathname || "/admin";

  const {
    register,
    handleSubmit,
    formState: { errors, isSubmitting },
  } = useForm({ resolver: zodResolver(schema) });

  const onSubmit = async (values) => {
    setServerError(null);
    try {
      await adminLogin(values.email, values.password);
      navigate(from, { replace: true });
    } catch (err) {
      setServerError(err?.message || "Invalid credentials");
    }
  };

  return (
    <div className="grid min-h-screen place-items-center bg-navy-950 px-4">
      <div className="w-full max-w-md">
        <div className="mb-6 flex items-center justify-center gap-2 text-white">
          <ShieldCheck className="size-6 text-gold-400" />
          <span className="text-lg font-semibold">Hirkani Exim · Admin Portal</span>
        </div>
        <div className="rounded-2xl border border-white/10 bg-navy-900 p-8 shadow-card">
          <h1 className="text-xl font-bold text-white">Staff sign in</h1>
          <p className="mt-1 text-sm text-navy-300">
            Platform administrators and support only.
          </p>
          <form onSubmit={handleSubmit(onSubmit)} className="mt-6 space-y-5 [&_label]:text-navy-200">
            {serverError && (
              <div className="rounded-lg border border-destructive/40 bg-destructive/15 px-4 py-3 text-sm text-red-200">
                {serverError}
              </div>
            )}
            <FormField label="Email" error={errors.email?.message} required>
              <Input type="email" placeholder="admin@hirkaniexim.com" {...register("email")} />
            </FormField>
            <FormField label="Password" error={errors.password?.message} required>
              <Input type="password" placeholder="••••••••" {...register("password")} />
            </FormField>
            <Button type="submit" variant="accent" size="lg" className="w-full" disabled={isSubmitting}>
              {isSubmitting ? "Signing in…" : <>Sign in <ArrowRight /></>}
            </Button>
          </form>
        </div>
      </div>
    </div>
  );
}
