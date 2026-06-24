import { Building2, BadgeCheck, ShieldAlert } from "lucide-react";
import { useAuth } from "@/auth/AuthContext";
import { useApiQuery } from "@/hooks/useApi";
import { getCompany } from "@/api/identity";
import { PageHeader } from "@/components/common/PageHeader";
import { LoadingState } from "@/components/common/LoadingState";
import { ErrorState } from "@/components/common/ErrorState";
import { Card, CardContent } from "@/components/ui/card";
import { Badge } from "@/components/ui/badge";
import { labelize } from "@/data/enums";

function Field({ label, value }) {
  return (
    <div>
      <dt className="text-sm text-muted-foreground">{label}</dt>
      <dd className="mt-0.5 font-medium text-navy-900">{value || "—"}</dd>
    </div>
  );
}

export default function Company() {
  const { user } = useAuth();
  const companyId = user?.companyId;

  const {
    data: company,
    isLoading,
    error,
    refetch,
  } = useApiQuery(["company", companyId], () => getCompany(companyId), {
    enabled: Boolean(companyId),
  });

  return (
    <div className="space-y-8">
      <PageHeader
        title="Company & profile"
        description="Your account and registered company details."
      />

      {/* My account */}
      <Card>
        <CardContent className="p-6">
          <h2 className="text-sm font-semibold uppercase tracking-wide text-muted-foreground">
            My account
          </h2>
          <dl className="mt-4 grid gap-5 sm:grid-cols-2 lg:grid-cols-4">
            <Field label="Name" value={user?.name} />
            <Field label="Email" value={user?.email} />
            <Field label="Role" value={labelize(user?.role)} />
            <Field label="Phone" value={user?.phone} />
          </dl>
        </CardContent>
      </Card>

      {/* Company */}
      {!companyId ? (
        <Card>
          <CardContent className="p-6 text-muted-foreground">
            Your account isn't linked to a company.
          </CardContent>
        </Card>
      ) : isLoading ? (
        <LoadingState />
      ) : error ? (
        <ErrorState error={error} onRetry={refetch} />
      ) : (
        <Card>
          <CardContent className="p-6">
            <div className="flex items-center justify-between">
              <div className="flex items-center gap-3">
                <span className="grid size-11 place-items-center rounded-xl bg-secondary text-primary">
                  <Building2 className="size-6" />
                </span>
                <div>
                  <h2 className="text-lg font-semibold text-navy-900">{company.name}</h2>
                  <p className="text-sm text-muted-foreground">{labelize(company.type)}</p>
                </div>
              </div>
              {company.verified ? (
                <Badge variant="teal">
                  <BadgeCheck className="size-3.5" /> Verified
                </Badge>
              ) : (
                <Badge variant="accent">
                  <ShieldAlert className="size-3.5" /> Not verified
                </Badge>
              )}
            </div>

            <dl className="mt-6 grid gap-5 sm:grid-cols-2 lg:grid-cols-4">
              <Field label="Country" value={company.country} />
              <Field label="IEC code" value={company.iecCode} />
              <Field label="GSTIN" value={company.gstin} />
              <Field label="Type" value={labelize(company.type)} />
            </dl>

            {!company.verified && (
              <div className="mt-6 rounded-lg border border-gold-400/40 bg-gold-400/10 px-4 py-3 text-sm text-navy-800">
                Complete KYC verification to unlock trading. (Verification module ships in
                Phase 2.)
              </div>
            )}
          </CardContent>
        </Card>
      )}
    </div>
  );
}
