import { useParams, useNavigate, Link } from "react-router-dom";
import { ArrowLeft, Search, Package, Sparkles } from "lucide-react";
import { useApiQuery } from "@/hooks/useApi";
import { getRfq, getRfqMatches } from "@/api/sourcing";
import { formatMoney, formatDate } from "@/lib/format";
import { LoadingState } from "@/components/common/LoadingState";
import { ErrorState } from "@/components/common/ErrorState";
import { DataTable } from "@/components/common/DataTable";
import { PageHeader } from "@/components/common/PageHeader";
import { StatusBadge } from "@/components/common/StatusBadge";
import { Card, CardContent } from "@/components/ui/card";
import { Badge } from "@/components/ui/badge";

function Field({ label, value }) {
  return (
    <div>
      <dt className="text-sm text-muted-foreground">{label}</dt>
      <dd className="mt-0.5 font-medium text-navy-900">{value ?? "—"}</dd>
    </div>
  );
}

export default function RfqDetail() {
  const { rfqId } = useParams();
  const navigate = useNavigate();

  const rfqQuery = useApiQuery(["rfq", rfqId], () => getRfq(rfqId));
  const matchesQuery = useApiQuery(["rfq-matches", rfqId], () => getRfqMatches(rfqId));

  const rfq = rfqQuery.data;
  const matches = matchesQuery.data;

  const columns = [
    { key: "name", header: "Product", render: (m) => <span className="font-medium">{m.name}</span> },
    { key: "hsCode", header: "HS code", render: (m) => <Badge variant="outline">{m.hsCode}</Badge> },
    { key: "unitPrice", header: "Unit price", render: (m) => formatMoney(m.unitPrice, m.currency) },
    { key: "unit", header: "Unit", render: (m) => m.unit || "—" },
  ];

  return (
    <div className="space-y-6">
      <Link
        to="/app/sourcing"
        className="inline-flex items-center gap-1.5 text-sm font-medium text-muted-foreground hover:text-navy-800"
      >
        <ArrowLeft className="size-4" /> Back to RFQs
      </Link>

      {rfqQuery.isLoading ? (
        <LoadingState />
      ) : rfqQuery.error ? (
        <ErrorState error={rfqQuery.error} onRetry={rfqQuery.refetch} />
      ) : (
        <>
          <div className="flex flex-wrap items-start justify-between gap-4">
            <div className="flex items-start gap-4">
              <span className="grid size-12 place-items-center rounded-xl bg-secondary text-primary">
                <Search className="size-6" />
              </span>
              <div>
                <h1 className="text-2xl font-bold text-navy-900">{rfq.title}</h1>
                <div className="mt-1.5 flex items-center gap-2">
                  <Badge variant="outline">HS {rfq.hsCode}</Badge>
                  <StatusBadge status={rfq.status} />
                </div>
              </div>
            </div>
          </div>

          <Card>
            <CardContent className="p-6">
              <dl className="grid gap-5 sm:grid-cols-2 lg:grid-cols-4">
                <Field label="Quantity" value={`${rfq.quantity} ${rfq.unit || ""}`.trim()} />
                <Field label="Target price" value={formatMoney(rfq.targetPrice, rfq.currency)} />
                <Field label="Currency" value={rfq.currency} />
                <Field label="Posted" value={formatDate(rfq.createdAt)} />
              </dl>
              {rfq.description && (
                <div className="mt-6">
                  <dt className="text-sm text-muted-foreground">Description</dt>
                  <p className="mt-1 leading-relaxed text-navy-800">{rfq.description}</p>
                </div>
              )}
            </CardContent>
          </Card>

          {/* Matched products from the catalog (cross-service lookup) */}
          <div>
            <div className="mb-3 flex items-center gap-2">
              <Sparkles className="size-5 text-gold-500" />
              <h2 className="text-lg font-semibold">
                Matching products
                {matches ? <span className="text-muted-foreground"> ({matches.count})</span> : null}
              </h2>
            </div>
            <DataTable
              columns={columns}
              rows={matches?.matches}
              isLoading={matchesQuery.isLoading}
              error={matchesQuery.error}
              onRetry={matchesQuery.refetch}
              rowKey="id"
              onRowClick={(m) => navigate(`/app/catalog/${m.id}`)}
              empty={{
                icon: Package,
                title: "No matching products",
                description: `No catalog products currently match HS code ${rfq.hsCode}.`,
              }}
            />
          </div>
        </>
      )}
    </div>
  );
}
