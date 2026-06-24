import { useState } from "react";
import { useParams, useNavigate, Link } from "react-router-dom";
import { ArrowLeft, Search, Package, Sparkles, ScrollText, Plus } from "lucide-react";
import { useAuth } from "@/auth/AuthContext";
import { useApiQuery } from "@/hooks/useApi";
import { getRfq, getRfqMatches } from "@/api/sourcing";
import { listQuotes } from "@/api/quotation";
import { formatMoney, formatDate } from "@/lib/format";
import { LoadingState } from "@/components/common/LoadingState";
import { ErrorState } from "@/components/common/ErrorState";
import { DataTable } from "@/components/common/DataTable";
import { PageHeader } from "@/components/common/PageHeader";
import { StatusBadge } from "@/components/common/StatusBadge";
import { Card, CardContent } from "@/components/ui/card";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { QuoteFormDialog } from "@/pages/app/quotes/QuoteFormDialog";

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
  const { user } = useAuth();
  const [quoting, setQuoting] = useState(false);

  const rfqQuery = useApiQuery(["rfq", rfqId], () => getRfq(rfqId));
  const matchesQuery = useApiQuery(["rfq-matches", rfqId], () => getRfqMatches(rfqId));
  const quotesQuery = useApiQuery(["rfq-quotes", rfqId], () =>
    listQuotes({ rfqId, size: 50 })
  );

  const rfq = rfqQuery.data;
  const matches = matchesQuery.data;
  const quotes = quotesQuery.data?.items || [];
  // The RFQ owner is the buyer; any other company can quote on it.
  const canQuote = rfq && user?.companyId && user.companyId !== rfq.buyerCompanyId;

  const columns = [
    { key: "name", header: "Product", render: (m) => <span className="font-medium">{m.name}</span> },
    { key: "hsCode", header: "HS code", render: (m) => <Badge variant="outline">{m.hsCode}</Badge> },
    { key: "unitPrice", header: "Unit price", render: (m) => formatMoney(m.unitPrice, m.currency) },
    { key: "unit", header: "Unit", render: (m) => m.unit || "—" },
  ];

  const quoteColumns = [
    {
      key: "productName",
      header: "Product",
      render: (q) => <span className="font-medium">{q.productName || "—"}</span>,
    },
    { key: "quantity", header: "Qty", render: (q) => `${q.quantity} ${q.unit || ""}`.trim() },
    { key: "unitPrice", header: "Unit price", render: (q) => formatMoney(q.unitPrice, q.currency) },
    {
      key: "total",
      header: "Total",
      render: (q) => formatMoney((q.unitPrice || 0) * (q.quantity || 0), q.currency),
    },
    { key: "status", header: "Status", render: (q) => <StatusBadge status={q.status} /> },
    { key: "createdAt", header: "Submitted", render: (q) => formatDate(q.createdAt) },
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

          {/* Quotes submitted against this RFQ */}
          <div>
            <div className="mb-3 flex items-center justify-between gap-2">
              <div className="flex items-center gap-2">
                <ScrollText className="size-5 text-primary" />
                <h2 className="text-lg font-semibold">
                  Quotes
                  <span className="text-muted-foreground"> ({quotes.length})</span>
                </h2>
              </div>
              {canQuote && (
                <Button variant="accent" size="sm" onClick={() => setQuoting(true)}>
                  <Plus /> Submit quote
                </Button>
              )}
            </div>
            <DataTable
              columns={quoteColumns}
              rows={quotes}
              isLoading={quotesQuery.isLoading}
              error={quotesQuery.error}
              onRetry={quotesQuery.refetch}
              rowKey="id"
              onRowClick={(q) => navigate(`/app/quotes/${q.id}`)}
              empty={{
                icon: ScrollText,
                title: "No quotes yet",
                description: canQuote
                  ? "Be the first to quote on this request."
                  : "Sellers haven't responded to this RFQ yet.",
              }}
            />
          </div>
        </>
      )}

      {quoting && rfq && (
        <QuoteFormDialog
          rfq={rfq}
          onClose={() => setQuoting(false)}
          onSaved={(quote) => {
            quotesQuery.refetch();
            setQuoting(false);
            if (quote?.id) navigate(`/app/quotes/${quote.id}`);
          }}
        />
      )}
    </div>
  );
}
