import { useState } from "react";
import { Link, useNavigate, useLocation } from "react-router-dom";
import { useForm } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import { z } from "zod";
import { ArrowRight } from "lucide-react";
import { useAuth } from "@/auth/AuthContext";
import { AuthLayout } from "./AuthLayout";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { FormField } from "@/components/common/FormField";

const schema = z.object({
  email: z.string().email("Enter a valid email"),
  password: z.string().min(1, "Password is required"),
});

export default function Login() {
  const { login } = useAuth();
  const navigate = useNavigate();
  const location = useLocation();
  const [serverError, setServerError] = useState(null);
  const from = location.state?.from?.pathname || "/app";

  const {
    register,
    handleSubmit,
    formState: { errors, isSubmitting },
  } = useForm({ resolver: zodResolver(schema) });

  const onSubmit = async (values) => {
    setServerError(null);
    try {
      await login(values.email, values.password);
      navigate(from, { replace: true });
    } catch (err) {
      setServerError(err?.message || "Invalid credentials");
    }
  };

  return (
    <AuthLayout
      title="Welcome back"
      subtitle="Sign in to your Hirkani Exim account."
      footer={
        <>
          New to Hirkani Exim?{" "}
          <Link to="/register" className="font-semibold text-primary hover:underline">
            Create an account
          </Link>
        </>
      }
    >
      <form onSubmit={handleSubmit(onSubmit)} className="space-y-5">
        {serverError && (
          <div className="rounded-lg border border-destructive/30 bg-destructive/10 px-4 py-3 text-sm text-destructive">
            {serverError}
          </div>
        )}
        <FormField label="Work email" error={errors.email?.message} required>
          <Input type="email" placeholder="you@company.com" {...register("email")} />
        </FormField>
        <FormField label="Password" error={errors.password?.message} required>
          <Input type="password" placeholder="••••••••" {...register("password")} />
        </FormField>
        <Button type="submit" variant="accent" size="lg" className="w-full" disabled={isSubmitting}>
          {isSubmitting ? "Signing in…" : <>Sign in <ArrowRight /></>}
        </Button>
      </form>
    </AuthLayout>
  );
}
