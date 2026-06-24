import { useParams, Link } from "react-router-dom";
import { useQueryClient } from "@tanstack/react-query";
import { ArrowLeft, Wallet, Banknote, CheckCircle2, Undo2 } from "lucide-react";
import { useAuth } from "@/auth/AuthContext";
import { useApiQuery, useApiMutation } from "@/hooks/useApi";
import { getPayment, fundPayment, releasePayment, refundPayment } from "@/api/payments";
import { formatMoney, formatDateTime } from "@/lib/format";
import { LoadingState } from "@/components/common/LoadingState";
import { ErrorState } from "@/components/common/ErrorState";
import { PageHeader } from "@/components/common/PageHeader";
import { StatusBadge } from "@/components/common/StatusBadge";
import { Card, CardContent } from "@/components/ui/card";
import { Button } from "@/components/ui/button";

function Field({ label, value }) {
  return (
    <div>
      <dt className="text-sm text-muted-foreground">{label}</dt>
      <dd className="mt-0.5 font-medium text-navy-900">{value ?? "—"}</dd>
    </div>
  );
}

export default function PaymentDetail() {
  const { paymentId } = useParams();
  const qc = useQueryClient();
  const { hasRole } = useAuth();

  const { data: txn, isLoading, error, refetch } = useApiQuery(
    ["payment", paymentId],
    () => getPayment(paymentId)
  );

  const invalidate = () => {
    qc.invalidateQueries({ queryKey: ["payment", paymentId] });
    qc.invalidateQueries({ queryKey: ["payments"] });
  };

  const fundMutation = useApiMutation(() => fundPayment(paymentId), {
    successMessage: "Escrow funded",
    onSuccess: invalidate,
  });
  const releaseMutation = useApiMutation(() => releasePayment(paymentId), {
    successMessage: "Escrow released to seller",
    onSuccess: invalidate,
  });
  const refundMutation = useApiMutation(() => refundPayment(paymentId), {
    successMessage: "Escrow refunded to buyer",
    onSuccess: invalidate,
  });

  const canSettle = hasRole("COMPANY_ADMIN", "PLATFORM_ADMIN"); // backend gates release/refund
  const isInitiated = txn?.status === "INITIATED";
  const isFunded = txn?.status === "FUNDED";

  return (
    <div className="space-y-6">
      <Link
        to="/app/payments"
        className="inline-flex items-center gap-1.5 text-sm font-medium text-muted-foreground hover:text-navy-800"
      >
        <ArrowLeft className="size-4" /> Back to payments
      </Link>

      {isLoading ? (
        <LoadingState />
      ) : error ? (
        <ErrorState error={error} onRetry={refetch} />
      ) : (
        <>
          <PageHeader
            title={
              <span className="flex items-center gap-3">
                <span className="grid size-10 place-items-center rounded-xl bg-secondary text-primary">
                  <Wallet className="size-5" />
                </span>
                Escrow {txn.id?.slice(0, 8)}
              </span>
            }
            actions={
              <div className="flex flex-wrap gap-2">
                {isInitiated && (
                  <Button variant="accent" onClick={() => fundMutation.mutate()} disabled={fundMutation.isPending}>
                    <Banknote /> Fund
                  </Button>
                )}
                {isFunded && canSettle && (
                  <>
                    <Button variant="accent" onClick={() => releaseMutation.mutate()} disabled={releaseMutation.isPending}>
                      <CheckCircle2 /> Release
                    </Button>
                    <Button variant="outline" onClick={() => refundMutation.mutate()} disabled={refundMutation.isPending}>
                      <Undo2 /> Refund
                    </Button>
                  </>
                )}
              </div>
            }
          />

          <div className="flex items-center gap-2">
            <StatusBadge status={txn.status} />
            <Link
              to={`/app/orders/${txn.orderId}`}
              className="text-sm text-muted-foreground hover:text-navy-800 hover:underline"
            >
              for order {txn.orderId?.slice(0, 8)}
            </Link>
          </div>

          <Card>
            <CardContent className="p-6">
              <dl className="grid gap-5 sm:grid-cols-2 lg:grid-cols-4">
                <Field label="Amount" value={formatMoney(txn.amount, txn.currency)} />
                <Field label="Method" value={(txn.method || "—").replace(/_/g, " ")} />
                <Field label="Payment term" value={txn.paymentTermCode || "—"} />
                <Field label="External ref" value={txn.externalRef || "—"} />
                <Field label="Initiated" value={formatDateTime(txn.createdAt)} />
                <Field label="Updated" value={formatDateTime(txn.updatedAt)} />
              </dl>
            </CardContent>
          </Card>

          {isFunded && !canSettle && (
            <p className="text-sm text-muted-foreground">
              Funds are in escrow. Releasing or refunding requires a COMPANY_ADMIN.
            </p>
          )}
        </>
      )}
    </div>
  );
}
