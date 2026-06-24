import { useState } from "react";
import { useQueryClient } from "@tanstack/react-query";
import { ShieldCheck, BadgeCheck, Search, ScrollText, BarChart3, Check, X } from "lucide-react";
import { useApiQuery, useApiMutation } from "@/hooks/useApi";
import { getMetrics, listSanctions, screenSanctions, listAdminActions, approveKyc, rejectKyc } from "@/api/admin";
import { listAllVerifications } from "@/api/verification";
import { listCompanies } from "@/api/identity";
import { VERIFICATION_TYPE_LABELS } from "@/data/enums";
import { formatDateTime, formatDate } from "@/lib/format";
import { PageHeader } from "@/components/common/PageHeader";
import { DataTable } from "@/components/common/DataTable";
import { Pagination } from "@/components/common/Pagination";
import { LoadingState } from "@/components/common/LoadingState";
import { StatusBadge } from "@/components/common/StatusBadge";
import { FormField } from "@/components/common/FormField";
import { Card, CardContent } from "@/components/ui/card";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { Input, Select } from "@/components/ui/input";
import { Dialog } from "@/components/ui/dialog";
import { cn } from "@/lib/utils";

const TABS = [
  { key: "metrics", label: "Metrics", icon: BarChart3 },
  { key: "kyc", label: "KYC review", icon: BadgeCheck },
  { key: "sanctions", label: "Sanctions", icon: Search },
  { key: "audit", label: "Audit log", icon: ScrollText },
];

export default function AdminConsole() {
  const [tab, setTab] = useState("metrics");
  return (
    <div className="space-y-6">
      <PageHeader
        title="Admin console"
        description="Platform operations — KYC decisions, sanctions screening, metrics and audit."
        actions={<Badge variant="accent"><ShieldCheck className="mr-1 size-3.5" /> PLATFORM_ADMIN</Badge>}
      />
      <div className="flex gap-1 border-b border-border">
        {TABS.map((t) => (
          <button
            key={t.key}
            onClick={() => setTab(t.key)}
            className={cn(
              "inline-flex items-center gap-2 border-b-2 px-4 py-2.5 text-sm font-medium transition-colors",
              tab === t.key ? "border-primary text-navy-900" : "border-transparent text-muted-foreground hover:text-navy-800"
            )}
          >
            <t.icon className="size-4" /> {t.label}
          </button>
        ))}
      </div>
      {tab === "metrics" && <MetricsTab />}
      {tab === "kyc" && <KycTab />}
      {tab === "sanctions" && <SanctionsTab />}
      {tab === "audit" && <AuditTab />}
    </div>
  );
}

/* ---------------- Metrics ---------------- */

function MetricsTab() {
  const query = useApiQuery(["admin-metrics"], () => getMetrics());
  const m = query.data;
  if (query.isLoading) return <LoadingState />;
  const cards = [
    { label: "Sanctioned entities", value: m?.sanctionedEntities },
    { label: "Admin actions", value: m?.totalAdminActions },
    { label: "KYC approvals", value: m?.kycApprovals },
    { label: "KYC rejections", value: m?.kycRejections },
    { label: "Sanction screenings", value: m?.sanctionScreenings },
  ];
  return (
    <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-3">
      {cards.map((c) => (
        <Card key={c.label}>
          <CardContent className="p-6">
            <p className="text-sm text-muted-foreground">{c.label}</p>
            <p className="mt-1 text-3xl font-bold text-navy-900">{c.value ?? "—"}</p>
          </CardContent>
        </Card>
      ))}
    </div>
  );
}

/* ---------------- KYC review ---------------- */

function KycTab() {
  const [page, setPage] = useState(0);
  const [status, setStatus] = useState("PENDING");
  const [reviewing, setReviewing] = useState(null);

  const params = { page, size: 10, sort: "createdAt,desc" };
  if (status) params.status = status;

  const query = useApiQuery(["verifications", status, page], () => listAllVerifications(params));
  const companiesQuery = useApiQuery(["companies", "all"], () => listCompanies({ size: 200 }));
  const companyName = (id) =>
    (companiesQuery.data?.items || []).find((c) => c.id === id)?.name || id?.slice(0, 8);

  const data = query.data;

  const columns = [
    { key: "company", header: "Company", render: (v) => <span className="font-medium">{companyName(v.companyId)}</span> },
    { key: "type", header: "Type", render: (v) => <Badge variant="outline">{VERIFICATION_TYPE_LABELS[v.type] || v.type}</Badge> },
    {
      key: "fileUrl",
      header: "Document",
      render: (v) =>
        v.fileUrl ? (
          <a href={v.fileUrl} target="_blank" rel="noreferrer" onClick={(e) => e.stopPropagation()}
             className="text-sm text-primary hover:underline">View</a>
        ) : "—",
    },
    { key: "status", header: "Status", render: (v) => <StatusBadge status={v.status} /> },
    { key: "createdAt", header: "Submitted", render: (v) => formatDate(v.createdAt) },
    {
      key: "actions",
      header: "",
      align: "right",
      render: (v) => (
        <Button variant={v.status === "PENDING" ? "accent" : "outline"} size="sm" onClick={() => setReviewing(v)}>
          {v.status === "PENDING" ? "Review" : "View"}
        </Button>
      ),
    },
  ];

  return (
    <div className="space-y-4">
      <div className="flex items-center gap-2">
        <Select className="h-9 w-auto" value={status} onChange={(e) => { setStatus(e.target.value); setPage(0); }}>
          <option value="PENDING">Pending review</option>
          <option value="APPROVED">Approved</option>
          <option value="REJECTED">Rejected</option>
          <option value="">All</option>
        </Select>
      </div>
      <DataTable
        columns={columns}
        rows={data?.items}
        isLoading={query.isLoading}
        error={query.error}
        onRetry={query.refetch}
        rowKey="id"
        onRowClick={(v) => setReviewing(v)}
        empty={{
          icon: BadgeCheck,
          title: status === "PENDING" ? "Nothing awaiting review" : "No verifications",
          description: "Submitted KYC documents appear here for review.",
        }}
      />
      <Pagination page={page} totalPages={data?.totalPages} onChange={setPage} />

      {reviewing && (
        <KycReviewDialog
          verification={reviewing}
          companyName={companyName(reviewing.companyId)}
          onClose={() => setReviewing(null)}
          onDecided={() => { query.refetch(); setReviewing(null); }}
        />
      )}
    </div>
  );
}

function KycReviewDialog({ verification: v, companyName, onClose, onDecided }) {
  const qc = useQueryClient();
  const [note, setNote] = useState("");
  const onDecision = {
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: ["verifications"] });
      qc.invalidateQueries({ queryKey: ["admin-metrics"] });
      onDecided();
    },
  };
  const approve = useApiMutation(() => approveKyc(v.id, note), { successMessage: "KYC approved", ...onDecision });
  const reject = useApiMutation(() => rejectKyc(v.id, note), { successMessage: "KYC rejected", ...onDecision });
  const pending = v.status === "PENDING";

  return (
    <Dialog
      open
      onClose={onClose}
      title={`KYC — ${companyName}`}
      description={VERIFICATION_TYPE_LABELS[v.type] || v.type}
      footer={
        pending ? (
          <>
            <Button variant="outline" onClick={() => reject.mutate()} disabled={reject.isPending}>
              <X /> Reject
            </Button>
            <Button variant="accent" onClick={() => approve.mutate()} disabled={approve.isPending}>
              <Check /> Approve
            </Button>
          </>
        ) : (
          <Button variant="outline" onClick={onClose}>Close</Button>
        )
      }
    >
      <div className="space-y-4">
        <div className="flex items-center justify-between">
          <StatusBadge status={v.status} />
          {v.fileUrl ? (
            <a href={v.fileUrl} target="_blank" rel="noreferrer" className="text-sm font-medium text-primary hover:underline">
              View document ↗
            </a>
          ) : (
            <span className="text-sm text-muted-foreground">No document URL</span>
          )}
        </div>
        <dl className="grid gap-3 text-sm sm:grid-cols-2">
          <div><dt className="text-muted-foreground">Company</dt><dd className="font-medium">{companyName}</dd></div>
          <div><dt className="text-muted-foreground">Submitted</dt><dd>{formatDateTime(v.createdAt)}</dd></div>
          {v.reviewerNote && (
            <div className="sm:col-span-2"><dt className="text-muted-foreground">Reviewer note</dt><dd>{v.reviewerNote}</dd></div>
          )}
        </dl>
        {pending && (
          <FormField label="Reviewer note (optional)">
            <Input placeholder="Reason / remarks" value={note} onChange={(e) => setNote(e.target.value)} />
          </FormField>
        )}
      </div>
    </Dialog>
  );
}

/* ---------------- Sanctions ---------------- */

function SanctionsTab() {
  const [name, setName] = useState("");
  const listQuery = useApiQuery(["sanctions"], () => listSanctions({ size: 50 }));
  const screenMutation = useApiMutation((n) => screenSanctions(n));
  const result = screenMutation.data;

  const columns = [
    { key: "name", header: "Name", render: (s) => <span className="font-medium">{s.name}</span> },
    { key: "country", header: "Country", render: (s) => s.country || "—" },
    { key: "programme", header: "Programme", render: (s) => s.programme || "—" },
    { key: "reason", header: "Reason", render: (s) => s.reason || "—" },
  ];

  return (
    <div className="space-y-4">
      <Card>
        <CardContent className="p-6">
          <div className="flex flex-wrap items-end gap-3">
            <FormField label="Screen a name" className="flex-1 min-w-64">
              <Input
                placeholder="e.g. a company or individual name"
                value={name}
                onChange={(e) => setName(e.target.value)}
                onKeyDown={(e) => e.key === "Enter" && name.trim() && screenMutation.mutate(name.trim())}
              />
            </FormField>
            <Button variant="accent" onClick={() => screenMutation.mutate(name.trim())} disabled={!name.trim() || screenMutation.isPending}>
              <Search /> Screen
            </Button>
          </div>
          {result && (
            <div className={cn("mt-4 rounded-lg border p-3 text-sm", result.hit ? "border-destructive/40 bg-destructive/5" : "border-teal-500/40 bg-teal-500/5")}>
              {result.hit
                ? `⚠ ${result.count} match${result.count === 1 ? "" : "es"} for “${result.query}”.`
                : `✓ No matches for “${result.query}”.`}
            </div>
          )}
        </CardContent>
      </Card>

      <div>
        <h3 className="mb-3 text-sm font-semibold text-navy-700">Denied-party list (seeded)</h3>
        <DataTable
          columns={columns}
          rows={listQuery.data?.items}
          isLoading={listQuery.isLoading}
          error={listQuery.error}
          onRetry={listQuery.refetch}
          rowKey="id"
          empty={{ icon: Search, title: "No sanctioned entities seeded" }}
        />
      </div>
    </div>
  );
}

/* ---------------- Audit log ---------------- */

function AuditTab() {
  const query = useApiQuery(["admin-actions"], () => listAdminActions({ size: 50 }));
  const columns = [
    { key: "type", header: "Action", render: (a) => <Badge variant="outline">{(a.type || "").replace(/_/g, " ")}</Badge> },
    { key: "actor", header: "Actor", render: (a) => a.actor || "—" },
    { key: "target", header: "Target", render: (a) => <span className="font-mono text-xs">{a.target || "—"}</span> },
    { key: "detail", header: "Detail", render: (a) => a.detail || "—" },
    { key: "createdAt", header: "When", render: (a) => formatDateTime(a.createdAt) },
  ];
  return (
    <DataTable
      columns={columns}
      rows={query.data?.items}
      isLoading={query.isLoading}
      error={query.error}
      onRetry={query.refetch}
      rowKey="id"
      empty={{ icon: ScrollText, title: "No admin actions yet" }}
    />
  );
}
