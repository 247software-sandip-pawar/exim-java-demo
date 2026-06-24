import { useState } from "react";
import { useForm } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import { z } from "zod";
import { useQueryClient } from "@tanstack/react-query";
import { AlertOctagon, Plus } from "lucide-react";
import { useAuth } from "@/auth/AuthContext";
import { useApiQuery, useApiMutation } from "@/hooks/useApi";
import { listDisputes, createDispute, updateDisputeStatus } from "@/api/trust";
import { listOrders } from "@/api/orders";
import { listCompanies } from "@/api/identity";
import { DISPUTE_STATUSES, DISPUTE_STATUS_NEXT } from "@/data/enums";
import { formatDate, formatDateTime } from "@/lib/format";
import { PageHeader } from "@/components/common/PageHeader";
import { DataTable } from "@/components/common/DataTable";
import { Pagination } from "@/components/common/Pagination";
import { StatusBadge } from "@/components/common/StatusBadge";
import { FormField } from "@/components/common/FormField";
import { Button } from "@/components/ui/button";
import { Input, Select, Textarea } from "@/components/ui/input";
import { Dialog } from "@/components/ui/dialog";

const PAGE_SIZE = 10;

export default function Disputes() {
  const { user, hasRole } = useAuth();
  const qc = useQueryClient();
  const [page, setPage] = useState(0);
  const [status, setStatus] = useState("");
  const [mineOnly, setMineOnly] = useState(false);
  const [raising, setRaising] = useState(false);
  const [selected, setSelected] = useState(null);

  const params = { page, size: PAGE_SIZE };
  if (status) params.status = status;

  const query = useApiQuery(["disputes", page, status], () => listDisputes(params));
  const rows = (query.data?.items || []).filter(
    (d) =>
      !mineOnly ||
      d.raisedByCompanyId === user?.companyId ||
      d.againstCompanyId === user?.companyId
  );

  const columns = [
    { key: "reason", header: "Reason", render: (d) => <span className="font-medium">{d.reason}</span> },
    {
      key: "orderId",
      header: "Order",
      render: (d) => <span className="font-mono text-xs text-navy-700">{d.orderId?.slice(0, 8)}</span>,
    },
    { key: "status", header: "Status", render: (d) => <StatusBadge status={d.status} /> },
    { key: "createdAt", header: "Raised", render: (d) => formatDate(d.createdAt) },
  ];

  return (
    <div className="space-y-6">
      <PageHeader
        title="Disputes"
        description="Raise and track disputes over orders. Support advances them to resolution."
        actions={
          <Button variant="accent" onClick={() => setRaising(true)}>
            <Plus /> Raise dispute
          </Button>
        }
      />

      <div className="flex flex-wrap items-center gap-2">
        <Button
          variant={mineOnly ? "default" : "outline"}
          size="sm"
          onClick={() => setMineOnly((v) => !v)}
        >
          {mineOnly ? "Showing mine" : "My disputes only"}
        </Button>
        <Select
          className="h-9 w-auto"
          value={status}
          onChange={(e) => {
            setStatus(e.target.value);
            setPage(0);
          }}
        >
          <option value="">All statuses</option>
          {DISPUTE_STATUSES.map((s) => (
            <option key={s} value={s}>{s.replace(/_/g, " ")}</option>
          ))}
        </Select>
      </div>

      <DataTable
        columns={columns}
        rows={rows}
        isLoading={query.isLoading}
        error={query.error}
        onRetry={query.refetch}
        rowKey="id"
        onRowClick={(d) => setSelected(d)}
        empty={{
          icon: AlertOctagon,
          title: "No disputes",
          description: "Raise a dispute if something goes wrong with an order.",
        }}
      />
      {!mineOnly && <Pagination page={page} totalPages={query.data?.totalPages} onChange={setPage} />}

      {raising && (
        <RaiseDisputeDialog
          onClose={() => setRaising(false)}
          onSaved={() => {
            query.refetch();
            setRaising(false);
          }}
        />
      )}
      {selected && (
        <DisputeDetailDialog
          dispute={selected}
          canManage={hasRole("PLATFORM_ADMIN", "SUPPORT")}
          onClose={() => setSelected(null)}
          onSaved={() => {
            qc.invalidateQueries({ queryKey: ["disputes"] });
            setSelected(null);
          }}
        />
      )}
    </div>
  );
}

const schema = z.object({
  orderId: z.string().uuid("Pick an order"),
  againstCompanyId: z.string().uuid("Pick a company"),
  reason: z.string().min(3, "Reason is required"),
  description: z.string().optional(),
});

function RaiseDisputeDialog({ onClose, onSaved }) {
  const { user } = useAuth();
  const ordersQuery = useApiQuery(["orders", "picker"], () => listOrders({ size: 100 }));
  const companiesQuery = useApiQuery(["companies", "picker"], () => listCompanies({ size: 100 }));
  const orders = (ordersQuery.data?.items || []).filter(
    (o) => o.buyerCompanyId === user?.companyId || o.sellerCompanyId === user?.companyId
  );
  const companies = (companiesQuery.data?.items || []).filter((c) => c.id !== user?.companyId);

  const {
    register,
    handleSubmit,
    formState: { errors },
  } = useForm({ resolver: zodResolver(schema) });

  const mutation = useApiMutation(
    (values) => createDispute({ ...values, raisedByCompanyId: user?.companyId }),
    { successMessage: "Dispute raised", onSuccess: onSaved }
  );

  return (
    <Dialog
      open
      onClose={onClose}
      title="Raise a dispute"
      description="Open a dispute against a counterparty over an order."
      footer={
        <>
          <Button variant="outline" onClick={onClose}>Cancel</Button>
          <Button
            variant="accent"
            onClick={handleSubmit((v) => mutation.mutate(v))}
            disabled={mutation.isPending}
          >
            {mutation.isPending ? "Raising…" : "Raise dispute"}
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
        <FormField label="Against company" error={errors.againstCompanyId?.message} required>
          <Select {...register("againstCompanyId")}>
            <option value="">Select a company…</option>
            {companies.map((c) => (
              <option key={c.id} value={c.id}>{c.name}</option>
            ))}
          </Select>
        </FormField>
        <FormField label="Reason" error={errors.reason?.message} required>
          <Input placeholder="e.g. Goods not as described" {...register("reason")} />
        </FormField>
        <FormField label="Description" error={errors.description?.message}>
          <Textarea rows={3} {...register("description")} />
        </FormField>
      </form>
    </Dialog>
  );
}

function DisputeDetailDialog({ dispute, canManage, onClose, onSaved }) {
  const allowedNext = DISPUTE_STATUS_NEXT[dispute.status] || [];
  const [status, setStatus] = useState(allowedNext[0] || "");
  const [resolution, setResolution] = useState("");

  const mutation = useApiMutation(
    () => updateDisputeStatus(dispute.id, { status, resolution: resolution || undefined }),
    { successMessage: "Dispute updated", onSuccess: onSaved }
  );

  return (
    <Dialog
      open
      onClose={onClose}
      title={dispute.reason}
      description={`Dispute over order ${dispute.orderId?.slice(0, 8)}`}
      footer={
        <>
          <Button variant="outline" onClick={onClose}>Close</Button>
          {canManage && allowedNext.length > 0 && (
            <Button variant="accent" disabled={!status || mutation.isPending} onClick={() => mutation.mutate()}>
              {mutation.isPending ? "Updating…" : "Update status"}
            </Button>
          )}
        </>
      }
    >
      <div className="space-y-4">
        <div className="flex items-center gap-2">
          <StatusBadge status={dispute.status} />
          <span className="text-xs text-muted-foreground">Raised {formatDateTime(dispute.createdAt)}</span>
        </div>
        {dispute.description && <p className="text-sm text-navy-800">{dispute.description}</p>}
        {dispute.resolution && (
          <div className="rounded-lg bg-secondary/60 p-3 text-sm">
            <p className="font-semibold">Resolution</p>
            <p className="text-navy-800">{dispute.resolution}</p>
          </div>
        )}
        {canManage && allowedNext.length > 0 && (
          <>
            <FormField label="Advance to">
              <Select value={status} onChange={(e) => setStatus(e.target.value)}>
                {allowedNext.map((s) => (
                  <option key={s} value={s}>{s.replace(/_/g, " ")}</option>
                ))}
              </Select>
            </FormField>
            <FormField label="Resolution note">
              <Textarea rows={2} value={resolution} onChange={(e) => setResolution(e.target.value)} />
            </FormField>
          </>
        )}
        {!canManage && allowedNext.length > 0 && (
          <p className="text-sm text-muted-foreground">Support will review and advance this dispute.</p>
        )}
      </div>
    </Dialog>
  );
}
