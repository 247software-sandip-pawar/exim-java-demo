import { useState } from "react";
import { useNavigate } from "react-router-dom";
import { useForm } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import { z } from "zod";
import { useQueryClient } from "@tanstack/react-query";
import { Plus, Search } from "lucide-react";
import { useAuth } from "@/auth/AuthContext";
import { useApiQuery, useApiMutation } from "@/hooks/useApi";
import { listRfqs, createRfq } from "@/api/sourcing";
import { formatMoney, formatDate } from "@/lib/format";
import { PageHeader } from "@/components/common/PageHeader";
import { DataTable } from "@/components/common/DataTable";
import { Pagination } from "@/components/common/Pagination";
import { StatusBadge } from "@/components/common/StatusBadge";
import { FormField } from "@/components/common/FormField";
import { Button } from "@/components/ui/button";
import { Badge } from "@/components/ui/badge";
import { Input, Textarea } from "@/components/ui/input";
import { Dialog } from "@/components/ui/dialog";

const PAGE_SIZE = 10;

export default function Rfqs() {
  const { user, hasRole } = useAuth();
  const navigate = useNavigate();
  const qc = useQueryClient();
  const canCreate = hasRole("COMPANY_ADMIN", "COMPANY_MEMBER", "PLATFORM_ADMIN");

  const [page, setPage] = useState(0);
  const [mineOnly, setMineOnly] = useState(false);
  const [creating, setCreating] = useState(false);

  const query = useApiQuery(["rfqs", page], () => listRfqs({ page, size: PAGE_SIZE }));
  const data = query.data;
  const rows = (data?.items || []).filter(
    (r) => !mineOnly || r.buyerCompanyId === user?.companyId
  );

  const columns = [
    { key: "title", header: "Title", render: (r) => <span className="font-medium">{r.title}</span> },
    { key: "hsCode", header: "HS code", render: (r) => <Badge variant="outline">{r.hsCode}</Badge> },
    { key: "quantity", header: "Qty", render: (r) => `${r.quantity} ${r.unit || ""}`.trim() },
    { key: "targetPrice", header: "Target", render: (r) => formatMoney(r.targetPrice, r.currency) },
    { key: "status", header: "Status", render: (r) => <StatusBadge status={r.status} /> },
    { key: "createdAt", header: "Posted", render: (r) => formatDate(r.createdAt) },
  ];

  return (
    <div className="space-y-6">
      <PageHeader
        title="Sourcing / RFQs"
        description="Browse buyer requests for quotation, or post your own."
        actions={
          canCreate && (
            <Button variant="accent" onClick={() => setCreating(true)}>
              <Plus /> New RFQ
            </Button>
          )
        }
      />

      <div className="flex items-center gap-2">
        <Button
          variant={mineOnly ? "default" : "outline"}
          size="sm"
          onClick={() => setMineOnly((v) => !v)}
        >
          {mineOnly ? "Showing my RFQs" : "My RFQs only"}
        </Button>
      </div>

      <DataTable
        columns={columns}
        rows={rows}
        isLoading={query.isLoading}
        error={query.error}
        onRetry={query.refetch}
        onRowClick={(r) => navigate(`/app/sourcing/${r.id}`)}
        empty={{
          icon: Search,
          title: mineOnly ? "You haven't posted any RFQs" : "No RFQs yet",
          description: canCreate ? "Post an RFQ to source from verified sellers." : undefined,
        }}
      />
      {!mineOnly && (
        <Pagination page={page} totalPages={data?.totalPages} onChange={setPage} />
      )}

      {creating && (
        <RfqFormDialog
          onClose={() => setCreating(false)}
          onSaved={(rfq) => {
            qc.invalidateQueries({ queryKey: ["rfqs"] });
            setCreating(false);
            if (rfq?.id) navigate(`/app/sourcing/${rfq.id}`);
          }}
        />
      )}
    </div>
  );
}

const schema = z.object({
  title: z.string().min(2, "Title is required"),
  description: z.string().optional(),
  hsCode: z.string().min(2, "HS code is required"),
  quantity: z.coerce.number().int().positive("Must be greater than 0"),
  unit: z.string().min(1, "e.g. MT, kg, pcs"),
  targetPrice: z.coerce.number().nonnegative().optional(),
  currency: z.string().min(3).max(3),
});

function RfqFormDialog({ onClose, onSaved }) {
  const { user } = useAuth();
  const {
    register,
    handleSubmit,
    formState: { errors },
  } = useForm({ resolver: zodResolver(schema), defaultValues: { currency: "USD" } });

  const mutation = useApiMutation(
    (values) => createRfq({ ...values, buyerCompanyId: user?.companyId }),
    { successMessage: "RFQ posted", onSuccess: onSaved }
  );

  return (
    <Dialog
      open
      onClose={onClose}
      title="Post an RFQ"
      description="Describe what you want to source. Matching sellers can respond with quotes."
      footer={
        <>
          <Button variant="outline" onClick={onClose}>Cancel</Button>
          <Button
            variant="accent"
            onClick={handleSubmit((v) => mutation.mutate(v))}
            disabled={mutation.isPending}
          >
            {mutation.isPending ? "Posting…" : "Post RFQ"}
          </Button>
        </>
      }
    >
      <form className="space-y-4" onSubmit={handleSubmit((v) => mutation.mutate(v))}>
        <FormField label="Title" error={errors.title?.message} required>
          <Input placeholder="e.g. Basmati rice — 24 MT" {...register("title")} />
        </FormField>
        <FormField label="Description" error={errors.description?.message}>
          <Textarea rows={3} {...register("description")} />
        </FormField>
        <div className="grid gap-4 sm:grid-cols-2">
          <FormField label="HS code" error={errors.hsCode?.message} required>
            <Input placeholder="1006.30" {...register("hsCode")} />
          </FormField>
          <FormField label="Unit" error={errors.unit?.message} required>
            <Input placeholder="MT" {...register("unit")} />
          </FormField>
        </div>
        <div className="grid gap-4 sm:grid-cols-3">
          <FormField label="Quantity" error={errors.quantity?.message} required>
            <Input type="number" {...register("quantity")} />
          </FormField>
          <FormField label="Target price" error={errors.targetPrice?.message}>
            <Input type="number" step="0.01" {...register("targetPrice")} />
          </FormField>
          <FormField label="Currency" error={errors.currency?.message} required>
            <Input maxLength={3} {...register("currency")} />
          </FormField>
        </div>
      </form>
    </Dialog>
  );
}
