import { useQueryClient } from "@tanstack/react-query";
import { Check, CreditCard, Wallet } from "lucide-react";
import { useAuth } from "@/auth/AuthContext";
import { useApiQuery, useApiMutation } from "@/hooks/useApi";
import {
  listPlans,
  listSubscriptions,
  subscribe,
  cancelSubscription,
  listPlatformPayments,
  payPlatformPayment,
} from "@/api/billing";
import { formatMoney, formatDate } from "@/lib/format";
import { PageHeader } from "@/components/common/PageHeader";
import { DataTable } from "@/components/common/DataTable";
import { LoadingState } from "@/components/common/LoadingState";
import { StatusBadge } from "@/components/common/StatusBadge";
import { Card, CardContent } from "@/components/ui/card";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { cn } from "@/lib/utils";

export default function Billing() {
  const { user, hasRole } = useAuth();
  const qc = useQueryClient();
  const companyId = user?.companyId;
  const canManage = hasRole("COMPANY_ADMIN", "PLATFORM_ADMIN");

  const plansQuery = useApiQuery(["plans"], () => listPlans({ size: 50 }));
  const subsQuery = useApiQuery(
    ["subscriptions", companyId],
    () => listSubscriptions({ companyId, size: 50 }),
    { enabled: Boolean(companyId) }
  );
  const paymentsQuery = useApiQuery(
    ["platform-payments", companyId],
    () => listPlatformPayments({ companyId, size: 50 }),
    { enabled: Boolean(companyId) }
  );

  const plans = plansQuery.data?.items || [];
  const activeSub = (subsQuery.data?.items || []).find((s) => s.status === "ACTIVE");

  const invalidate = () => {
    qc.invalidateQueries({ queryKey: ["subscriptions"] });
    qc.invalidateQueries({ queryKey: ["platform-payments"] });
  };

  const subscribeMutation = useApiMutation(
    (planCode) => subscribe({ companyId, planCode, autoRenew: true }),
    { successMessage: "Subscribed", onSuccess: invalidate }
  );
  const cancelMutation = useApiMutation((id) => cancelSubscription(id), {
    successMessage: "Subscription cancelled",
    onSuccess: invalidate,
  });
  const payMutation = useApiMutation((id) => payPlatformPayment(id), {
    successMessage: "Payment completed",
    onSuccess: invalidate,
  });

  const paymentColumns = [
    { key: "amount", header: "Amount", render: (p) => formatMoney(p.amount, p.currency) },
    { key: "status", header: "Status", render: (p) => <StatusBadge status={p.status} /> },
    { key: "createdAt", header: "Date", render: (p) => formatDate(p.createdAt) },
    {
      key: "action",
      header: "",
      align: "right",
      render: (p) =>
        p.status === "PENDING" && canManage ? (
          <Button
            size="sm"
            variant="accent"
            disabled={payMutation.isPending}
            onClick={() => payMutation.mutate(p.id)}
          >
            Pay now
          </Button>
        ) : null,
    },
  ];

  return (
    <div className="space-y-6">
      <PageHeader
        title="Subscription & billing"
        description="Choose a plan, manage your subscription, and settle platform invoices."
      />

      {/* Current subscription */}
      {subsQuery.isLoading ? (
        <LoadingState />
      ) : activeSub ? (
        <Card>
          <CardContent className="flex flex-wrap items-center justify-between gap-4 p-6">
            <div>
              <p className="text-sm text-muted-foreground">Current plan</p>
              <div className="mt-1 flex items-center gap-2">
                <h2 className="text-xl font-bold text-navy-900">{activeSub.planCode}</h2>
                <StatusBadge status={activeSub.status} />
              </div>
              <p className="mt-1 text-sm text-muted-foreground">
                Renews {formatDate(activeSub.currentPeriodEnd)} · auto-renew{" "}
                {activeSub.autoRenew ? "on" : "off"}
              </p>
            </div>
            {canManage && (
              <Button
                variant="outline"
                disabled={cancelMutation.isPending}
                onClick={() => cancelMutation.mutate(activeSub.id)}
              >
                Cancel subscription
              </Button>
            )}
          </CardContent>
        </Card>
      ) : (
        <p className="text-sm text-muted-foreground">
          No active subscription. Pick a plan below to get started.
        </p>
      )}

      {/* Plans */}
      <div>
        <h2 className="mb-3 text-lg font-semibold">Plans</h2>
        {plansQuery.isLoading ? (
          <LoadingState />
        ) : (
          <div className="grid gap-4 md:grid-cols-3">
            {plans.map((plan) => {
              const current = activeSub?.planCode === plan.code;
              return (
                <Card key={plan.id} className={cn(current && "ring-2 ring-primary")}>
                  <CardContent className="flex h-full flex-col p-6">
                    <div className="flex items-center justify-between">
                      <h3 className="text-lg font-bold text-navy-900">{plan.name}</h3>
                      {current && <Badge variant="teal">Current</Badge>}
                    </div>
                    <p className="mt-1 text-sm text-muted-foreground">{plan.description}</p>
                    <p className="mt-4 text-2xl font-bold text-navy-900">
                      {formatMoney(plan.priceMonthly, plan.currency)}
                      <span className="text-sm font-normal text-muted-foreground">/mo</span>
                    </p>
                    <ul className="mt-4 space-y-1.5 text-sm text-navy-700">
                      <li className="flex items-center gap-2">
                        <Check className="size-4 text-teal-600" /> Up to {plan.maxProducts} products
                      </li>
                      <li className="flex items-center gap-2">
                        <Check className="size-4 text-teal-600" /> Up to {plan.maxRfqs} RFQs
                      </li>
                    </ul>
                    <div className="mt-auto pt-5">
                      <Button
                        className="w-full"
                        variant={current ? "outline" : "accent"}
                        disabled={current || !canManage || subscribeMutation.isPending}
                        onClick={() => subscribeMutation.mutate(plan.code)}
                      >
                        {current ? "Active" : "Subscribe"}
                      </Button>
                    </div>
                  </CardContent>
                </Card>
              );
            })}
          </div>
        )}
      </div>

      {/* Platform payments */}
      <div>
        <h2 className="mb-3 flex items-center gap-2 text-lg font-semibold">
          <Wallet className="size-5 text-primary" /> Platform payments
        </h2>
        <DataTable
          columns={paymentColumns}
          rows={paymentsQuery.data?.items}
          isLoading={paymentsQuery.isLoading}
          error={paymentsQuery.error}
          onRetry={paymentsQuery.refetch}
          rowKey="id"
          empty={{
            icon: CreditCard,
            title: "No platform payments",
            description: "Subscription invoices appear here.",
          }}
        />
      </div>
    </div>
  );
}
