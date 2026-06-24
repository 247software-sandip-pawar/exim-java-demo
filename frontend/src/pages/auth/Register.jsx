import { useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import { useForm } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import { z } from "zod";
import { ArrowRight } from "lucide-react";
import { useAuth } from "@/auth/AuthContext";
import { AuthLayout } from "./AuthLayout";
import { Button } from "@/components/ui/button";
import { Input, Select } from "@/components/ui/input";
import { FormField } from "@/components/common/FormField";

const COMPANY_TYPES = ["EXPORTER", "IMPORTER", "BOTH", "CHA", "FREIGHT_FORWARDER"];

const schema = z.object({
  companyName: z.string().min(2, "Company name is required"),
  companyType: z.enum(COMPANY_TYPES, { message: "Select a company type" }),
  country: z.string().min(2, "Country is required"),
  adminName: z.string().min(2, "Your name is required"),
  email: z.string().email("Enter a valid email"),
  password: z.string().min(8, "At least 8 characters"),
});

export default function Register() {
  const { register: registerUser } = useAuth();
  const navigate = useNavigate();
  const [serverError, setServerError] = useState(null);

  const {
    register,
    handleSubmit,
    formState: { errors, isSubmitting },
  } = useForm({ resolver: zodResolver(schema), defaultValues: { companyType: "" } });

  const onSubmit = async (values) => {
    setServerError(null);
    try {
      await registerUser(values);
      navigate("/app", { replace: true });
    } catch (err) {
      setServerError(err?.message || "Registration failed");
    }
  };

  return (
    <AuthLayout
      title="Create your company account"
      subtitle="Register your business to start trading on Hirkani Exim."
      footer={
        <>
          Already have an account?{" "}
          <Link to="/login" className="font-semibold text-primary hover:underline">
            Sign in
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
        <FormField label="Company name" error={errors.companyName?.message} required>
          <Input placeholder="Acme Trading Co." {...register("companyName")} />
        </FormField>
        <div className="grid gap-5 sm:grid-cols-2">
          <FormField label="Company type" error={errors.companyType?.message} required>
            <Select {...register("companyType")} defaultValue="">
              <option value="" disabled>Select…</option>
              {COMPANY_TYPES.map((t) => (
                <option key={t} value={t}>{t.replace(/_/g, " ")}</option>
              ))}
            </Select>
          </FormField>
          <FormField label="Country" error={errors.country?.message} required>
            <Input placeholder="India" {...register("country")} />
          </FormField>
        </div>
        <FormField label="Your name" error={errors.adminName?.message} required>
          <Input placeholder="Jane Doe" {...register("adminName")} />
        </FormField>
        <FormField label="Work email" error={errors.email?.message} required>
          <Input type="email" placeholder="you@company.com" {...register("email")} />
        </FormField>
        <FormField label="Password" error={errors.password?.message} hint="Minimum 8 characters" required>
          <Input type="password" placeholder="••••••••" {...register("password")} />
        </FormField>
        <Button type="submit" variant="accent" size="lg" className="w-full" disabled={isSubmitting}>
          {isSubmitting ? "Creating account…" : <>Create account <ArrowRight /></>}
        </Button>
      </form>
    </AuthLayout>
  );
}
