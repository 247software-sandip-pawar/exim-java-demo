import { useState } from "react";
import { useNavigate } from "react-router-dom";
import { ScrollText } from "lucide-react";
import { useAuth } from "@/auth/AuthContext";
import { useApiQuery } from "@/hooks/useApi";
import { listQuotes } from "@/api/quotation";
import { QUOTE_STATUSES } from "@/data/enums";
import { formatMoney, formatDate } from "@/lib/format";
import { PageHeader } from "@/components/common/PageHeader";
import { DataTable } from "@/components/common/DataTable";
import { Pagination } from "@/components/common/Pagination";
import { StatusBadge } from "@/components/common/StatusBadge";
import { Button } from "@/components/ui/button";
import { Select } from "@/components/ui/input";

const PAGE_SIZE = 10;

export default function Quotes() {
  const { user } = useAuth();
  const navigate = useNavigate();
  const [page, setPage] = useState(0);
  const [status, setStatus] = useState("");
  const [mineOnly, setMineOnly] = useState(false);

  const params = { page, size: PAGE_SIZE };
  if (status) params.status = status;

  const query = useApiQuery(["quotes", page, status], () => listQuotes(params));
  const data = query.data;
  const rows = (data?.items || []).filter(
    (q) =>
      !mineOnly ||
      q.sellerCompanyId === user?.companyId ||
      q.buyerCompanyId === user?.companyId
  );

  const columns = [
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
    { key: "incoterm", header: "Incoterm", render: (q) => q.incoterm || "—" },
    { key: "status", header: "Status", render: (q) => <StatusBadge status={q.status} /> },
    { key: "createdAt", header: "Submitted", render: (q) => formatDate(q.createdAt) },
  ];

  return (
    <div className="space-y-6">
      <PageHeader
        title="Quotes"
        description="Quotes submitted against RFQs. Submit a new quote from an RFQ's detail page."
      />

      <div className="flex flex-wrap items-center gap-2">
        <Button
          variant={mineOnly ? "default" : "outline"}
          size="sm"
          onClick={() => setMineOnly((v) => !v)}
        >
          {mineOnly ? "Showing my quotes" : "My quotes only"}
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
          {QUOTE_STATUSES.map((s) => (
            <option key={s} value={s}>{s}</option>
          ))}
        </Select>
      </div>

      <DataTable
        columns={columns}
        rows={rows}
        isLoading={query.isLoading}
        error={query.error}
        onRetry={query.refetch}
        onRowClick={(q) => navigate(`/app/quotes/${q.id}`)}
        empty={{
          icon: ScrollText,
          title: "No quotes yet",
          description: "Quotes appear here once sellers respond to RFQs.",
        }}
      />
      {!mineOnly && <Pagination page={page} totalPages={data?.totalPages} onChange={setPage} />}
    </div>
  );
}
