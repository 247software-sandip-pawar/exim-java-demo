import { useState } from "react";
import { useForm } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import { z } from "zod";
import { useQueryClient } from "@tanstack/react-query";
import { Star, Plus } from "lucide-react";
import { useAuth } from "@/auth/AuthContext";
import { useApiQuery, useApiMutation } from "@/hooks/useApi";
import { listRatings, createRating, getRatingSummary } from "@/api/trust";
import { listOrders } from "@/api/orders";
import { listCompanies } from "@/api/identity";
import { formatDate } from "@/lib/format";
import { PageHeader } from "@/components/common/PageHeader";
import { DataTable } from "@/components/common/DataTable";
import { FormField } from "@/components/common/FormField";
import { Card, CardContent } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { Input, Select, Textarea } from "@/components/ui/input";
import { Dialog } from "@/components/ui/dialog";
import { cn } from "@/lib/utils";

function Stars({ value, size = "size-4" }) {
  return (
    <span className="inline-flex">
      {[1, 2, 3, 4, 5].map((n) => (
        <Star
          key={n}
          className={cn(size, n <= Math.round(value) ? "fill-gold-400 text-gold-500" : "text-border")}
        />
      ))}
    </span>
  );
}

export default function Ratings() {
  const { user } = useAuth();
  const [companyId, setCompanyId] = useState(user?.companyId || "");
  const [rating, setRating] = useState(false);

  const companiesQuery = useApiQuery(["companies", "picker"], () => listCompanies({ size: 100 }));
  const companies = companiesQuery.data?.items || [];

  const summaryQuery = useApiQuery(
    ["rating-summary", companyId],
    () => getRatingSummary(companyId),
    { enabled: Boolean(companyId) }
  );
  const listQuery = useApiQuery(
    ["ratings", companyId],
    () => listRatings({ ratedCompanyId: companyId, size: 50 }),
    { enabled: Boolean(companyId) }
  );

  const summary = summaryQuery.data;
  const companyName = (id) => companies.find((c) => c.id === id)?.name || id?.slice(0, 8);

  const columns = [
    {
      key: "score",
      header: "Score",
      render: (r) => <Stars value={r.score} />,
    },
    { key: "comment", header: "Comment", render: (r) => r.comment || "—" },
    { key: "rater", header: "By", render: (r) => companyName(r.raterCompanyId) },
    { key: "createdAt", header: "Date", render: (r) => formatDate(r.createdAt) },
  ];

  return (
    <div className="space-y-6">
      <PageHeader
        title="Ratings"
        description="See a company's reputation and rate the counterparties you've traded with."
        actions={
          <Button variant="accent" onClick={() => setRating(true)}>
            <Plus /> Rate a company
          </Button>
        }
      />

      <FormField label="Company">
        <Select value={companyId} onChange={(e) => setCompanyId(e.target.value)} className="max-w-md">
          <option value="">Select a company…</option>
          {companies.map((c) => (
            <option key={c.id} value={c.id}>{c.name}</option>
          ))}
        </Select>
      </FormField>

      {companyId && (
        <>
          <Card>
            <CardContent className="flex items-center gap-6 p-6">
              <div>
                <p className="text-4xl font-bold text-navy-900">
                  {summary ? summary.averageScore.toFixed(1) : "—"}
                </p>
                <Stars value={summary?.averageScore || 0} size="size-5" />
              </div>
              <div className="text-sm text-muted-foreground">
                Based on {summary?.count ?? 0} rating{summary?.count === 1 ? "" : "s"}
              </div>
            </CardContent>
          </Card>

          <DataTable
            columns={columns}
            rows={listQuery.data?.items}
            isLoading={listQuery.isLoading}
            error={listQuery.error}
            onRetry={listQuery.refetch}
            rowKey="id"
            empty={{ icon: Star, title: "No ratings yet", description: "This company hasn't been rated." }}
          />
        </>
      )}

      {rating && (
        <RateDialog
          companies={companies.filter((c) => c.id !== user?.companyId)}
          onClose={() => setRating(false)}
          onSaved={() => {
            summaryQuery.refetch();
            listQuery.refetch();
            setRating(false);
          }}
        />
      )}
    </div>
  );
}

const schema = z.object({
  orderId: z.string().uuid("Pick an order"),
  ratedCompanyId: z.string().uuid("Pick a company"),
  score: z.coerce.number().int().min(1).max(5),
  comment: z.string().optional(),
});

function RateDialog({ companies, onClose, onSaved }) {
  const { user } = useAuth();
  const qc = useQueryClient();
  const ordersQuery = useApiQuery(["orders", "picker"], () => listOrders({ size: 100 }));
  const orders = (ordersQuery.data?.items || []).filter(
    (o) => o.buyerCompanyId === user?.companyId || o.sellerCompanyId === user?.companyId
  );

  const {
    register,
    handleSubmit,
    formState: { errors },
  } = useForm({ resolver: zodResolver(schema), defaultValues: { score: 5 } });

  const mutation = useApiMutation(
    (values) => createRating({ ...values, raterCompanyId: user?.companyId }),
    {
      successMessage: "Rating submitted",
      onSuccess: () => {
        qc.invalidateQueries({ queryKey: ["ratings"] });
        qc.invalidateQueries({ queryKey: ["rating-summary"] });
        onSaved();
      },
    }
  );

  return (
    <Dialog
      open
      onClose={onClose}
      title="Rate a company"
      description="Rate a counterparty for a completed order."
      footer={
        <>
          <Button variant="outline" onClick={onClose}>Cancel</Button>
          <Button
            variant="accent"
            onClick={handleSubmit((v) => mutation.mutate(v))}
            disabled={mutation.isPending}
          >
            {mutation.isPending ? "Submitting…" : "Submit rating"}
          </Button>
        </>
      }
    >
      <form className="space-y-4" onSubmit={handleSubmit((v) => mutation.mutate(v))}>
        <FormField
          label="Order"
          error={errors.orderId?.message}
          required
          hint={ordersQuery.isLoading ? "Loading orders…" : orders.length === 0 ? "No orders yet." : undefined}
        >
          <Select {...register("orderId")} disabled={orders.length === 0}>
            <option value="">Select an order…</option>
            {orders.map((o) => (
              <option key={o.id} value={o.id}>
                {o.id.slice(0, 8)} — {o.items?.[0]?.description || "order"}
              </option>
            ))}
          </Select>
        </FormField>
        <FormField label="Company" error={errors.ratedCompanyId?.message} required>
          <Select {...register("ratedCompanyId")}>
            <option value="">Select a company…</option>
            {companies.map((c) => (
              <option key={c.id} value={c.id}>{c.name}</option>
            ))}
          </Select>
        </FormField>
        <FormField label="Score (1–5)" error={errors.score?.message} required>
          <Select {...register("score")}>
            {[5, 4, 3, 2, 1].map((n) => (
              <option key={n} value={n}>{n} star{n === 1 ? "" : "s"}</option>
            ))}
          </Select>
        </FormField>
        <FormField label="Comment" error={errors.comment?.message}>
          <Textarea rows={2} placeholder="Optional feedback" {...register("comment")} />
        </FormField>
      </form>
    </Dialog>
  );
}
