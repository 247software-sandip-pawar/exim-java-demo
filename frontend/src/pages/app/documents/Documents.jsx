import { useState } from "react";
import { useNavigate } from "react-router-dom";
import { FileText, ExternalLink } from "lucide-react";
import { useApiQuery } from "@/hooks/useApi";
import { listDocuments } from "@/api/documents";
import { DOCUMENT_TYPE_LABELS } from "@/data/enums";
import { formatMoney, formatDate } from "@/lib/format";
import { PageHeader } from "@/components/common/PageHeader";
import { DataTable } from "@/components/common/DataTable";
import { Pagination } from "@/components/common/Pagination";
import { StatusBadge } from "@/components/common/StatusBadge";

const PAGE_SIZE = 10;

export default function Documents() {
  const navigate = useNavigate();
  const [page, setPage] = useState(0);

  const query = useApiQuery(["documents", "all", page], () =>
    listDocuments({ page, size: PAGE_SIZE })
  );
  const data = query.data;

  const columns = [
    {
      key: "type",
      header: "Document",
      render: (d) => (
        <span className="font-medium">{DOCUMENT_TYPE_LABELS[d.type] || d.type}</span>
      ),
    },
    { key: "documentNumber", header: "Number", render: (d) => d.documentNumber || "—" },
    {
      key: "orderId",
      header: "Order",
      render: (d) => <span className="font-mono text-xs text-navy-700">{d.orderId?.slice(0, 8)}</span>,
    },
    { key: "totalAmount", header: "Amount", render: (d) => formatMoney(d.totalAmount, d.currency) },
    { key: "status", header: "Status", render: (d) => <StatusBadge status={d.status} /> },
    { key: "createdAt", header: "Generated", render: (d) => formatDate(d.createdAt) },
    {
      key: "fileUrl",
      header: "",
      render: (d) =>
        d.fileUrl ? (
          <a
            href={d.fileUrl}
            target="_blank"
            rel="noreferrer"
            onClick={(e) => e.stopPropagation()}
            className="inline-flex items-center gap-1 text-sm text-primary hover:underline"
          >
            Open <ExternalLink className="size-3.5" />
          </a>
        ) : null,
    },
  ];

  return (
    <div className="space-y-6">
      <PageHeader
        title="Documents"
        description="Trade documents generated across your orders. Generate new ones from an order's detail page."
      />
      <DataTable
        columns={columns}
        rows={data?.items}
        isLoading={query.isLoading}
        error={query.error}
        onRetry={query.refetch}
        rowKey="id"
        onRowClick={(d) => navigate(`/app/orders/${d.orderId}`)}
        empty={{
          icon: FileText,
          title: "No documents yet",
          description: "Generate a document from an order to see it here.",
        }}
      />
      <Pagination page={page} totalPages={data?.totalPages} onChange={setPage} />
    </div>
  );
}
