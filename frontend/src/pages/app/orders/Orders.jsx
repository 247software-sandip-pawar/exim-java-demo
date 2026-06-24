import { useState } from "react";
import { useNavigate } from "react-router-dom";
import { ShoppingCart } from "lucide-react";
import { useAuth } from "@/auth/AuthContext";
import { useApiQuery } from "@/hooks/useApi";
import { listOrders } from "@/api/orders";
import { ORDER_STATUSES } from "@/data/enums";
import { formatMoney, formatDate } from "@/lib/format";
import { PageHeader } from "@/components/common/PageHeader";
import { DataTable } from "@/components/common/DataTable";
import { Pagination } from "@/components/common/Pagination";
import { StatusBadge } from "@/components/common/StatusBadge";
import { Button } from "@/components/ui/button";
import { Select } from "@/components/ui/input";

const PAGE_SIZE = 10;

export default function Orders() {
  const { user } = useAuth();
  const navigate = useNavigate();
  const [page, setPage] = useState(0);
  const [status, setStatus] = useState("");
  const [mineOnly, setMineOnly] = useState(false);

  const params = { page, size: PAGE_SIZE };
  if (status) params.status = status;

  const query = useApiQuery(["orders", page, status], () => listOrders(params));
  const data = query.data;
  const rows = (data?.items || []).filter(
    (o) =>
      !mineOnly ||
      o.buyerCompanyId === user?.companyId ||
      o.sellerCompanyId === user?.companyId
  );

  const columns = [
    {
      key: "id",
      header: "Order",
      render: (o) => <span className="font-mono text-xs text-navy-700">{o.id?.slice(0, 8)}</span>,
    },
    {
      key: "items",
      header: "Items",
      render: (o) => o.items?.[0]?.description || `${o.items?.length || 0} item(s)`,
    },
    { key: "totalAmount", header: "Total", render: (o) => formatMoney(o.totalAmount, o.currency) },
    { key: "status", header: "Status", render: (o) => <StatusBadge status={o.status} /> },
    { key: "createdAt", header: "Created", render: (o) => formatDate(o.createdAt) },
  ];

  return (
    <div className="space-y-6">
      <PageHeader
        title="Orders"
        description="Orders are created automatically when a buyer accepts a quote."
      />

      <div className="flex flex-wrap items-center gap-2">
        <Button
          variant={mineOnly ? "default" : "outline"}
          size="sm"
          onClick={() => setMineOnly((v) => !v)}
        >
          {mineOnly ? "Showing my orders" : "My orders only"}
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
          {ORDER_STATUSES.map((s) => (
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
        onRowClick={(o) => navigate(`/app/orders/${o.id}`)}
        empty={{
          icon: ShoppingCart,
          title: "No orders yet",
          description: "Accept a quote on an RFQ to create your first order.",
        }}
      />
      {!mineOnly && <Pagination page={page} totalPages={data?.totalPages} onChange={setPage} />}
    </div>
  );
}
