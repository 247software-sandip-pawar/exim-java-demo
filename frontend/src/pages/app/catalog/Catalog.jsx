import { useState } from "react";
import { useNavigate } from "react-router-dom";
import { useQueryClient } from "@tanstack/react-query";
import { Plus, Pencil, Trash2, Package, Search, X } from "lucide-react";
import { useAuth } from "@/auth/AuthContext";
import { useApiQuery, useApiMutation } from "@/hooks/useApi";
import { listProducts, deleteProduct } from "@/api/catalog";
import { formatMoney } from "@/lib/format";
import { PageHeader } from "@/components/common/PageHeader";
import { DataTable } from "@/components/common/DataTable";
import { Pagination } from "@/components/common/Pagination";
import { ConfirmDialog } from "@/components/common/ConfirmDialog";
import { Button } from "@/components/ui/button";
import { Badge } from "@/components/ui/badge";
import { Input } from "@/components/ui/input";
import { ProductFormDialog } from "./ProductFormDialog";

const PAGE_SIZE = 10;

export default function Catalog() {
  const { user, hasRole } = useAuth();
  const navigate = useNavigate();
  const qc = useQueryClient();
  const canManage = hasRole("COMPANY_ADMIN", "COMPANY_MEMBER", "PLATFORM_ADMIN");

  const [page, setPage] = useState(0);
  const [hsInput, setHsInput] = useState("");
  const [hsCode, setHsCode] = useState("");
  const [editing, setEditing] = useState(null);
  const [deleting, setDeleting] = useState(null);

  const query = useApiQuery(["products", { hsCode, page }], () =>
    listProducts({ hsCode: hsCode || undefined, page, size: PAGE_SIZE })
  );
  const data = query.data;
  const invalidate = () => qc.invalidateQueries({ queryKey: ["products"] });

  const deleteMutation = useApiMutation((id) => deleteProduct(id), {
    successMessage: "Product deleted",
    onSuccess: () => {
      invalidate();
      setDeleting(null);
    },
  });

  const applySearch = () => {
    setPage(0);
    setHsCode(hsInput.trim());
  };

  const isMine = (p) => p.companyId === user?.companyId;

  const columns = [
    { key: "name", header: "Product", render: (p) => <span className="font-medium">{p.name}</span> },
    { key: "hsCode", header: "HS code", render: (p) => <Badge variant="outline">{p.hsCode}</Badge> },
    { key: "unitPrice", header: "Unit price", render: (p) => formatMoney(p.unitPrice, p.currency) },
    { key: "unit", header: "Unit" },
    { key: "minOrderQty", header: "MOQ", render: (p) => p.minOrderQty },
    {
      key: "active",
      header: "Status",
      render: (p) =>
        p.active ? <Badge variant="teal">Active</Badge> : <Badge variant="outline">Inactive</Badge>,
    },
    {
      key: "actions",
      header: "",
      align: "right",
      render: (p) =>
        canManage && isMine(p) ? (
          <div className="flex justify-end gap-1" onClick={(e) => e.stopPropagation()}>
            <Button variant="ghost" size="icon" onClick={() => setEditing(p)} aria-label="Edit">
              <Pencil />
            </Button>
            <Button variant="ghost" size="icon" onClick={() => setDeleting(p)} aria-label="Delete">
              <Trash2 className="text-destructive" />
            </Button>
          </div>
        ) : (
          <span className="text-xs text-muted-foreground">{isMine(p) ? "" : "—"}</span>
        ),
    },
  ];

  return (
    <div className="space-y-6">
      <PageHeader
        title="Catalog"
        description="Browse products across the marketplace, or manage your own listings."
        actions={
          canManage && (
            <Button variant="accent" onClick={() => setEditing({})}>
              <Plus /> New product
            </Button>
          )
        }
      />

      <div className="flex gap-2">
        <div className="relative flex-1 sm:max-w-xs">
          <Search className="absolute left-3 top-1/2 size-4 -translate-y-1/2 text-muted-foreground" />
          <Input
            className="pl-9"
            placeholder="Search by HS code…"
            value={hsInput}
            onChange={(e) => setHsInput(e.target.value)}
            onKeyDown={(e) => e.key === "Enter" && applySearch()}
          />
          {hsCode && (
            <button
              className="absolute right-2 top-1/2 -translate-y-1/2 text-muted-foreground hover:text-navy-800"
              onClick={() => {
                setHsInput("");
                setHsCode("");
                setPage(0);
              }}
              aria-label="Clear"
            >
              <X className="size-4" />
            </button>
          )}
        </div>
        <Button variant="outline" onClick={applySearch}>Search</Button>
      </div>

      <DataTable
        columns={columns}
        rows={data?.items}
        isLoading={query.isLoading}
        error={query.error}
        onRetry={query.refetch}
        onRowClick={(p) => navigate(`/app/catalog/${p.id}`)}
        empty={{
          icon: Package,
          title: hsCode ? `No products for HS ${hsCode}` : "No products yet",
          description: canManage ? "Add your first product to the catalog." : undefined,
        }}
      />
      <Pagination page={page} totalPages={data?.totalPages} onChange={setPage} />

      {editing && (
        <ProductFormDialog
          product={editing.id ? editing : null}
          onClose={() => setEditing(null)}
          onSaved={() => {
            invalidate();
            setEditing(null);
          }}
        />
      )}
      {deleting && (
        <ConfirmDialog
          title="Delete product"
          description={`This removes “${deleting.name}” from the catalog.`}
          confirmLabel="Delete"
          destructive
          pending={deleteMutation.isPending}
          onConfirm={() => deleteMutation.mutate(deleting.id)}
          onClose={() => setDeleting(null)}
        />
      )}
    </div>
  );
}
