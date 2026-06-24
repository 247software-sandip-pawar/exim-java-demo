import { useState } from "react";
import { useParams, useNavigate, Link } from "react-router-dom";
import { useForm } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import { z } from "zod";
import { useQueryClient } from "@tanstack/react-query";
import { ArrowLeft, ScrollText, Check, X, Repeat, ShoppingCart } from "lucide-react";
import { useAuth } from "@/auth/AuthContext";
import { useApiQuery, useApiMutation } from "@/hooks/useApi";
import { getQuote, acceptQuote, rejectQuote, counterQuote } from "@/api/quotation";
import { createOrder } from "@/api/orders";
import { INCOTERMS } from "@/data/enums";
import { formatMoney, formatDate, formatDateTime } from "@/lib/format";
import { LoadingState } from "@/components/common/LoadingState";
import { ErrorState } from "@/components/common/ErrorState";
import { PageHeader } from "@/components/common/PageHeader";
import { StatusBadge } from "@/components/common/StatusBadge";
import { FormField } from "@/components/common/FormField";
import { Card, CardContent } from "@/components/ui/card";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { Dialog } from "@/components/ui/dialog";
import { Input, Select, Textarea } from "@/components/ui/input";

function Field({ label, value }) {
  return (
    <div>
      <dt className="text-sm text-muted-foreground">{label}</dt>
      <dd className="mt-0.5 font-medium text-navy-900">{value ?? "—"}</dd>
    </div>
  );
}

export default function QuoteDetail() {
  const { quoteId } = useParams();
  const navigate = useNavigate();
  const qc = useQueryClient();
  const { user } = useAuth();
  const [countering, setCountering] = useState(false);

  const { data: quote, isLoading, error, refetch } = useApiQuery(
    ["quote", quoteId],
    () => getQuote(quoteId)
  );

  const invalidate = () => {
    qc.invalidateQueries({ queryKey: ["quote", quoteId] });
    qc.invalidateQueries({ queryKey: ["quotes"] });
  };

  const acceptMutation = useApiMutation(() => acceptQuote(quoteId), {
    successMessage: "Quote accepted",
    onSuccess: invalidate,
  });
  const rejectMutation = useApiMutation(() => rejectQuote(quoteId), {
    successMessage: "Quote rejected",
    onSuccess: invalidate,
  });
  const orderMutation = useApiMutation(() => createOrder(quoteId), {
    successMessage: "Order created",
    onSuccess: (order) => {
      qc.invalidateQueries({ queryKey: ["orders"] });
      if (order?.id) navigate(`/app/orders/${order.id}`);
    },
  });

  const isBuyer = user?.companyId === quote?.buyerCompanyId;
  const isSeller = user?.companyId === quote?.sellerCompanyId;
  const isOpen = quote?.status === "SUBMITTED";
  const isAccepted = quote?.status === "ACCEPTED";
  const total = (quote?.unitPrice || 0) * (quote?.quantity || 0);

  return (
    <div className="space-y-6">
      <Link
        to="/app/quotes"
        className="inline-flex items-center gap-1.5 text-sm font-medium text-muted-foreground hover:text-navy-800"
      >
        <ArrowLeft className="size-4" /> Back to quotes
      </Link>

      {isLoading ? (
        <LoadingState />
      ) : error ? (
        <ErrorState error={error} onRetry={refetch} />
      ) : (
        <>
          <PageHeader
            title={quote.productName || "Quote"}
            description={
              <>
                For{" "}
                <Link className="font-medium text-primary hover:underline" to={`/app/sourcing/${quote.rfqId}`}>
                  the requesting RFQ
                </Link>
              </>
            }
            actions={
              <div className="flex flex-wrap gap-2">
                {isOpen && isBuyer && (
                  <>
                    <Button
                      variant="accent"
                      onClick={() => acceptMutation.mutate()}
                      disabled={acceptMutation.isPending}
                    >
                      <Check /> Accept
                    </Button>
                    <Button
                      variant="outline"
                      onClick={() => rejectMutation.mutate()}
                      disabled={rejectMutation.isPending}
                    >
                      <X /> Reject
                    </Button>
                  </>
                )}
                {isOpen && (isBuyer || isSeller) && (
                  <Button variant="outline" onClick={() => setCountering(true)}>
                    <Repeat /> Counter
                  </Button>
                )}
                {isAccepted && (
                  <Button
                    variant="accent"
                    onClick={() => orderMutation.mutate()}
                    disabled={orderMutation.isPending}
                  >
                    <ShoppingCart /> {orderMutation.isPending ? "Creating…" : "Create order"}
                  </Button>
                )}
              </div>
            }
          />

          <div className="flex items-center gap-2">
            <span className="grid size-10 place-items-center rounded-xl bg-secondary text-primary">
              <ScrollText className="size-5" />
            </span>
            <StatusBadge status={quote.status} />
            {quote.parentQuoteId && (
              <Link
                className="text-sm text-muted-foreground hover:text-navy-800 hover:underline"
                to={`/app/quotes/${quote.parentQuoteId}`}
              >
                ← counter of an earlier quote
              </Link>
            )}
          </div>

          <Card>
            <CardContent className="p-6">
              <dl className="grid gap-5 sm:grid-cols-2 lg:grid-cols-4">
                <Field label="Quantity" value={`${quote.quantity} ${quote.unit || ""}`.trim()} />
                <Field label="Unit price" value={formatMoney(quote.unitPrice, quote.currency)} />
                <Field label="Total" value={formatMoney(total, quote.currency)} />
                <Field label="Incoterm" value={quote.incoterm || "—"} />
                <Field label="Valid until" value={formatDate(quote.validUntil)} />
                <Field label="Submitted" value={formatDateTime(quote.createdAt)} />
              </dl>
              {quote.notes && (
                <div className="mt-6">
                  <dt className="text-sm text-muted-foreground">Notes</dt>
                  <p className="mt-1 leading-relaxed text-navy-800">{quote.notes}</p>
                </div>
              )}
            </CardContent>
          </Card>

          {isAccepted && (
            <p className="text-sm text-muted-foreground">
              This quote is accepted. Creating an order is idempotent — one order per quote.
            </p>
          )}
        </>
      )}

      {countering && (
        <CounterQuoteDialog
          quote={quote}
          onClose={() => setCountering(false)}
          onSaved={(next) => {
            invalidate();
            setCountering(false);
            if (next?.id) navigate(`/app/quotes/${next.id}`);
          }}
        />
      )}
    </div>
  );
}

const counterSchema = z.object({
  quantity: z.coerce.number().int().positive("Must be greater than 0"),
  unit: z.string().optional(),
  unitPrice: z.coerce.number().positive("Enter a unit price"),
  currency: z.string().min(3).max(3),
  incoterm: z.string().optional(),
  validUntil: z.string().optional(),
  notes: z.string().optional(),
});

function CounterQuoteDialog({ quote, onClose, onSaved }) {
  const {
    register,
    handleSubmit,
    formState: { errors },
  } = useForm({
    resolver: zodResolver(counterSchema),
    defaultValues: {
      quantity: quote.quantity,
      unit: quote.unit || "",
      unitPrice: quote.unitPrice,
      currency: quote.currency || "USD",
      incoterm: quote.incoterm || "",
    },
  });

  const mutation = useApiMutation(
    (values) =>
      counterQuote(quote.id, {
        ...values,
        validUntil: values.validUntil ? new Date(values.validUntil).toISOString() : undefined,
      }),
    { successMessage: "Counter-offer sent", onSuccess: onSaved }
  );

  return (
    <Dialog
      open
      onClose={onClose}
      title="Counter-offer"
      description="Restate the commercial terms. This creates a new quote linked to this one."
      footer={
        <>
          <Button variant="outline" onClick={onClose}>Cancel</Button>
          <Button
            variant="accent"
            onClick={handleSubmit((v) => mutation.mutate(v))}
            disabled={mutation.isPending}
          >
            {mutation.isPending ? "Sending…" : "Send counter"}
          </Button>
        </>
      }
    >
      <form className="space-y-4" onSubmit={handleSubmit((v) => mutation.mutate(v))}>
        <div className="grid gap-4 sm:grid-cols-3">
          <FormField label="Quantity" error={errors.quantity?.message} required>
            <Input type="number" {...register("quantity")} />
          </FormField>
          <FormField label="Unit" error={errors.unit?.message}>
            <Input {...register("unit")} />
          </FormField>
          <FormField label="Incoterm" error={errors.incoterm?.message}>
            <Select {...register("incoterm")}>
              <option value="">—</option>
              {INCOTERMS.map((i) => (
                <option key={i} value={i}>{i}</option>
              ))}
            </Select>
          </FormField>
        </div>
        <div className="grid gap-4 sm:grid-cols-3">
          <FormField label="Unit price" error={errors.unitPrice?.message} required>
            <Input type="number" step="0.01" {...register("unitPrice")} />
          </FormField>
          <FormField label="Currency" error={errors.currency?.message} required>
            <Input maxLength={3} {...register("currency")} />
          </FormField>
          <FormField label="Valid until" error={errors.validUntil?.message}>
            <Input type="date" {...register("validUntil")} />
          </FormField>
        </div>
        <FormField label="Notes" error={errors.notes?.message}>
          <Textarea rows={2} {...register("notes")} />
        </FormField>
      </form>
    </Dialog>
  );
}
