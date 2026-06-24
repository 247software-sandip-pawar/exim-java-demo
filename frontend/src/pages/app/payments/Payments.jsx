import { useState } from "react";
import { useNavigate } from "react-router-dom";
import { useForm } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import { z } from "zod";
import { Wallet, Plus, Landmark, FileClock } from "lucide-react";
import { useAuth } from "@/auth/AuthContext";
import { useApiQuery, useApiMutation } from "@/hooks/useApi";
import {
  listPayments,
  initiatePayment,
  listLettersOfCredit,
  createLetterOfCredit,
  updateLcStatus,
  listPaymentTerms,
} from "@/api/payments";
import { listOrders } from "@/api/orders";
import { listCompanies } from "@/api/identity";
import { PAYMENT_METHODS, LC_STATUS_NEXT } from "@/data/enums";
import { formatMoney, formatDate } from "@/lib/format";
import { PageHeader } from "@/components/common/PageHeader";
import { DataTable } from "@/components/common/DataTable";
import { StatusBadge } from "@/components/common/StatusBadge";
import { FormField } from "@/components/common/FormField";
import { Button } from "@/components/ui/button";
import { Input, Select } from "@/components/ui/input";
import { Dialog } from "@/components/ui/dialog";
import { cn } from "@/lib/utils";

const TABS = [
  { key: "escrow", label: "Escrow", icon: Wallet },
  { key: "lc", label: "Letters of credit", icon: Landmark },
  { key: "terms", label: "Payment terms", icon: FileClock },
];

export default function Payments() {
  const [tab, setTab] = useState("escrow");

  return (
    <div className="space-y-6">
      <PageHeader
        title="Payments"
        description="Run escrow on an order, open letters of credit, and review payment terms."
      />

      <div className="flex gap-1 border-b border-border">
        {TABS.map((t) => (
          <button
            key={t.key}
            onClick={() => setTab(t.key)}
            className={cn(
              "inline-flex items-center gap-2 border-b-2 px-4 py-2.5 text-sm font-medium transition-colors",
              tab === t.key
                ? "border-primary text-navy-900"
                : "border-transparent text-muted-foreground hover:text-navy-800"
            )}
          >
            <t.icon className="size-4" /> {t.label}
          </button>
        ))}
      </div>

      {tab === "escrow" && <EscrowTab />}
      {tab === "lc" && <LcTab />}
      {tab === "terms" && <TermsTab />}
    </div>
  );
}

/* ---------------- Escrow ---------------- */

function EscrowTab() {
  const { user } = useAuth();
  const navigate = useNavigate();
  const [mineOnly, setMineOnly] = useState(false);
  const [initiating, setInitiating] = useState(false);

  const query = useApiQuery(["payments"], () => listPayments({ size: 100 }));
  const rows = (query.data?.items || []).filter(
    (t) =>
      !mineOnly ||
      t.payerCompanyId === user?.companyId ||
      t.payeeCompanyId === user?.companyId
  );

  const columns = [
    {
      key: "id",
      header: "Txn",
      render: (t) => <span className="font-mono text-xs text-navy-700">{t.id?.slice(0, 8)}</span>,
    },
    { key: "amount", header: "Amount", render: (t) => formatMoney(t.amount, t.currency) },
    { key: "method", header: "Method", render: (t) => (t.method || "—").replace(/_/g, " ") },
    { key: "paymentTermCode", header: "Term", render: (t) => t.paymentTermCode || "—" },
    { key: "status", header: "Status", render: (t) => <StatusBadge status={t.status} /> },
    { key: "createdAt", header: "Initiated", render: (t) => formatDate(t.createdAt) },
  ];

  return (
    <div className="space-y-4">
      <div className="flex items-center justify-between gap-2">
        <Button
          variant={mineOnly ? "default" : "outline"}
          size="sm"
          onClick={() => setMineOnly((v) => !v)}
        >
          {mineOnly ? "Showing mine" : "My transactions only"}
        </Button>
        <Button variant="accent" onClick={() => setInitiating(true)}>
          <Plus /> Initiate escrow
        </Button>
      </div>

      <DataTable
        columns={columns}
        rows={rows}
        isLoading={query.isLoading}
        error={query.error}
        onRetry={query.refetch}
        onRowClick={(t) => navigate(`/app/payments/${t.id}`)}
        empty={{
          icon: Wallet,
          title: "No transactions yet",
          description: "Initiate an escrow transaction for one of your orders.",
        }}
      />

      {initiating && (
        <InitiateEscrowDialog
          onClose={() => setInitiating(false)}
          onSaved={(t) => {
            query.refetch();
            setInitiating(false);
            if (t?.id) navigate(`/app/payments/${t.id}`);
          }}
        />
      )}
    </div>
  );
}

const escrowSchema = z.object({
  orderId: z.string().uuid("Pick an order"),
  method: z.enum(PAYMENT_METHODS),
  paymentTermCode: z.string().optional(),
});

function InitiateEscrowDialog({ onClose, onSaved }) {
  const { user } = useAuth();
  const ordersQuery = useApiQuery(["orders", "picker"], () => listOrders({ size: 100 }));
  const termsQuery = useApiQuery(["payment-terms"], () => listPaymentTerms({ size: 100 }));
  const orders = (ordersQuery.data?.items || []).filter(
    (o) => o.buyerCompanyId === user?.companyId || o.sellerCompanyId === user?.companyId
  );
  const terms = termsQuery.data?.items || [];

  const {
    register,
    handleSubmit,
    formState: { errors },
  } = useForm({ resolver: zodResolver(escrowSchema), defaultValues: { method: "ESCROW" } });

  const mutation = useApiMutation(
    (values) =>
      initiatePayment({
        orderId: values.orderId,
        method: values.method,
        paymentTermCode: values.paymentTermCode || undefined,
      }),
    { successMessage: "Escrow initiated", onSuccess: onSaved }
  );

  return (
    <Dialog
      open
      onClose={onClose}
      title="Initiate escrow"
      description="Amount and parties are snapshotted from the order."
      footer={
        <>
          <Button variant="outline" onClick={onClose}>Cancel</Button>
          <Button
            variant="accent"
            onClick={handleSubmit((v) => mutation.mutate(v))}
            disabled={mutation.isPending}
          >
            {mutation.isPending ? "Initiating…" : "Initiate"}
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
                {o.id.slice(0, 8)} — {formatMoney(o.totalAmount, o.currency)} ({o.status})
              </option>
            ))}
          </Select>
        </FormField>
        <div className="grid gap-4 sm:grid-cols-2">
          <FormField label="Method" error={errors.method?.message} required>
            <Select {...register("method")}>
              {PAYMENT_METHODS.map((m) => (
                <option key={m} value={m}>{m.replace(/_/g, " ")}</option>
              ))}
            </Select>
          </FormField>
          <FormField label="Payment term" error={errors.paymentTermCode?.message}>
            <Select {...register("paymentTermCode")}>
              <option value="">—</option>
              {terms.map((t) => (
                <option key={t.id} value={t.code}>{t.code} — {t.description}</option>
              ))}
            </Select>
          </FormField>
        </div>
      </form>
    </Dialog>
  );
}

/* ---------------- Letters of credit ---------------- */

function LcTab() {
  const [creating, setCreating] = useState(false);
  const [selected, setSelected] = useState(null);
  const query = useApiQuery(["letters-of-credit"], () => listLettersOfCredit({ size: 100 }));

  const columns = [
    { key: "lcNumber", header: "LC #", render: (l) => <span className="font-medium">{l.lcNumber || "—"}</span> },
    { key: "issuingBank", header: "Issuing bank", render: (l) => l.issuingBank || "—" },
    { key: "amount", header: "Amount", render: (l) => formatMoney(l.amount, l.currency) },
    { key: "expiryDate", header: "Expiry", render: (l) => formatDate(l.expiryDate) },
    { key: "status", header: "Status", render: (l) => <StatusBadge status={l.status} /> },
  ];

  return (
    <div className="space-y-4">
      <div className="flex justify-end">
        <Button variant="accent" onClick={() => setCreating(true)}>
          <Plus /> Open letter of credit
        </Button>
      </div>
      <DataTable
        columns={columns}
        rows={query.data?.items}
        isLoading={query.isLoading}
        error={query.error}
        onRetry={query.refetch}
        rowKey="id"
        onRowClick={(l) => setSelected(l)}
        empty={{
          icon: Landmark,
          title: "No letters of credit",
          description: "Open an LC to secure payment between bank-backed parties.",
        }}
      />
      {creating && (
        <CreateLcDialog
          onClose={() => setCreating(false)}
          onSaved={() => {
            query.refetch();
            setCreating(false);
          }}
        />
      )}
      {selected && (
        <LcStatusDialog
          lc={selected}
          onClose={() => setSelected(null)}
          onSaved={() => {
            query.refetch();
            setSelected(null);
          }}
        />
      )}
    </div>
  );
}

const lcSchema = z.object({
  beneficiaryCompanyId: z.string().uuid("Pick the beneficiary"),
  orderId: z.string().optional(),
  issuingBank: z.string().min(2, "Issuing bank is required"),
  advisingBank: z.string().optional(),
  amount: z.coerce.number().positive("Enter an amount"),
  currency: z.string().min(3).max(3),
  expiryDate: z.string().optional(),
});

function CreateLcDialog({ onClose, onSaved }) {
  const { user } = useAuth();
  const companiesQuery = useApiQuery(["companies", "picker"], () => listCompanies({ size: 100 }));
  const companies = (companiesQuery.data?.items || []).filter((c) => c.id !== user?.companyId);

  const {
    register,
    handleSubmit,
    formState: { errors },
  } = useForm({ resolver: zodResolver(lcSchema), defaultValues: { currency: "USD" } });

  const mutation = useApiMutation(
    (values) =>
      createLetterOfCredit({
        applicantCompanyId: user?.companyId,
        beneficiaryCompanyId: values.beneficiaryCompanyId,
        orderId: values.orderId || undefined,
        issuingBank: values.issuingBank,
        advisingBank: values.advisingBank || undefined,
        amount: values.amount,
        currency: values.currency,
        expiryDate: values.expiryDate ? new Date(values.expiryDate).toISOString() : undefined,
      }),
    { successMessage: "Letter of credit opened", onSuccess: onSaved }
  );

  return (
    <Dialog
      open
      onClose={onClose}
      title="Open letter of credit"
      description="You are the applicant (buyer). Choose the beneficiary (seller)."
      footer={
        <>
          <Button variant="outline" onClick={onClose}>Cancel</Button>
          <Button
            variant="accent"
            onClick={handleSubmit((v) => mutation.mutate(v))}
            disabled={mutation.isPending}
          >
            {mutation.isPending ? "Opening…" : "Open LC"}
          </Button>
        </>
      }
    >
      <form className="space-y-4" onSubmit={handleSubmit((v) => mutation.mutate(v))}>
        <FormField
          label="Beneficiary"
          error={errors.beneficiaryCompanyId?.message}
          required
          hint={companiesQuery.isLoading ? "Loading companies…" : undefined}
        >
          <Select {...register("beneficiaryCompanyId")}>
            <option value="">Select a company…</option>
            {companies.map((c) => (
              <option key={c.id} value={c.id}>{c.name}</option>
            ))}
          </Select>
        </FormField>
        <div className="grid gap-4 sm:grid-cols-2">
          <FormField label="Issuing bank" error={errors.issuingBank?.message} required>
            <Input placeholder="HSBC" {...register("issuingBank")} />
          </FormField>
          <FormField label="Advising bank" error={errors.advisingBank?.message}>
            <Input placeholder="Optional" {...register("advisingBank")} />
          </FormField>
        </div>
        <div className="grid gap-4 sm:grid-cols-3">
          <FormField label="Amount" error={errors.amount?.message} required>
            <Input type="number" step="0.01" {...register("amount")} />
          </FormField>
          <FormField label="Currency" error={errors.currency?.message} required>
            <Input maxLength={3} {...register("currency")} />
          </FormField>
          <FormField label="Expiry" error={errors.expiryDate?.message}>
            <Input type="date" {...register("expiryDate")} />
          </FormField>
        </div>
      </form>
    </Dialog>
  );
}

function LcStatusDialog({ lc, onClose, onSaved }) {
  const allowedNext = LC_STATUS_NEXT[lc.status] || [];
  const [status, setStatus] = useState(allowedNext[0] || "");

  const mutation = useApiMutation(() => updateLcStatus(lc.id, status), {
    successMessage: "LC status updated",
    onSuccess: onSaved,
  });

  return (
    <Dialog
      open
      onClose={onClose}
      title={lc.lcNumber || "Letter of credit"}
      description="Review and advance this letter of credit."
      footer={
        <>
          <Button variant="outline" onClick={onClose}>Close</Button>
          {allowedNext.length > 0 && (
            <Button variant="accent" disabled={!status || mutation.isPending} onClick={() => mutation.mutate()}>
              {mutation.isPending ? "Updating…" : "Update status"}
            </Button>
          )}
        </>
      }
    >
      <div className="space-y-4">
        <dl className="grid grid-cols-2 gap-4 text-sm">
          <div><dt className="text-muted-foreground">Amount</dt><dd className="font-medium">{formatMoney(lc.amount, lc.currency)}</dd></div>
          <div><dt className="text-muted-foreground">Status</dt><dd><StatusBadge status={lc.status} /></dd></div>
          <div><dt className="text-muted-foreground">Issuing bank</dt><dd className="font-medium">{lc.issuingBank || "—"}</dd></div>
          <div><dt className="text-muted-foreground">Advising bank</dt><dd className="font-medium">{lc.advisingBank || "—"}</dd></div>
          <div><dt className="text-muted-foreground">Expiry</dt><dd className="font-medium">{formatDate(lc.expiryDate)}</dd></div>
        </dl>
        {allowedNext.length > 0 ? (
          <FormField label="Advance to">
            <Select value={status} onChange={(e) => setStatus(e.target.value)}>
              {allowedNext.map((s) => (
                <option key={s} value={s}>{s}</option>
              ))}
            </Select>
          </FormField>
        ) : (
          <p className="text-sm text-muted-foreground">This letter of credit is in a terminal state.</p>
        )}
      </div>
    </Dialog>
  );
}

/* ---------------- Payment terms (reference) ---------------- */

function TermsTab() {
  const query = useApiQuery(["payment-terms"], () => listPaymentTerms({ size: 100 }));
  const columns = [
    { key: "code", header: "Code", render: (t) => <span className="font-medium">{t.code}</span> },
    { key: "description", header: "Description", render: (t) => t.description },
    { key: "netDays", header: "Net days", render: (t) => t.netDays },
  ];
  return (
    <DataTable
      columns={columns}
      rows={query.data?.items}
      isLoading={query.isLoading}
      error={query.error}
      onRetry={query.refetch}
      rowKey="id"
      empty={{ icon: FileClock, title: "No payment terms seeded" }}
    />
  );
}
