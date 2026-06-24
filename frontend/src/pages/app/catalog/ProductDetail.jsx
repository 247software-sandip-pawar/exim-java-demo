import { useState } from "react";
import { useParams, useNavigate, Link } from "react-router-dom";
import { useQueryClient } from "@tanstack/react-query";
import { ArrowLeft, Pencil, Trash2, Package } from "lucide-react";
import { useAuth } from "@/auth/AuthContext";
import { useApiQuery, useApiMutation } from "@/hooks/useApi";
import { getProduct, deleteProduct } from "@/api/catalog";
import { formatMoney, formatDate } from "@/lib/format";
import { LoadingState } from "@/components/common/LoadingState";
import { ErrorState } from "@/components/common/ErrorState";
import { ConfirmDialog } from "@/components/common/ConfirmDialog";
import { Card, CardContent } from "@/components/ui/card";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { ProductFormDialog } from "./ProductFormDialog";

function Field({ label, value }) {
  return (
    <div>
      <dt className="text-sm text-muted-foreground">{label}</dt>
      <dd className="mt-0.5 font-medium text-navy-900">{value ?? "—"}</dd>
    </div>
  );
}

export default function ProductDetail() {
  const { productId } = useParams();
  const navigate = useNavigate();
  const qc = useQueryClient();
  const { user, hasRole } = useAuth();
  const [editing, setEditing] = useState(false);
  const [deleting, setDeleting] = useState(false);

  const { data: product, isLoading, error, refetch } = useApiQuery(
    ["product", productId],
    () => getProduct(productId)
  );

  const deleteMutation = useApiMutation(() => deleteProduct(productId), {
    successMessage: "Product deleted",
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: ["products"] });
      navigate("/app/catalog");
    },
  });

  const canManage =
    hasRole("COMPANY_ADMIN", "COMPANY_MEMBER", "PLATFORM_ADMIN") &&
    product?.companyId === user?.companyId;

  return (
    <div className="space-y-6">
      <Link
        to="/app/catalog"
        className="inline-flex items-center gap-1.5 text-sm font-medium text-muted-foreground hover:text-navy-800"
      >
        <ArrowLeft className="size-4" /> Back to catalog
      </Link>

      {isLoading ? (
        <LoadingState />
      ) : error ? (
        <ErrorState error={error} onRetry={refetch} />
      ) : (
        <>
          <div className="flex flex-wrap items-start justify-between gap-4">
            <div className="flex items-start gap-4">
              <span className="grid size-12 place-items-center rounded-xl bg-secondary text-primary">
                <Package className="size-6" />
              </span>
              <div>
                <h1 className="text-2xl font-bold text-navy-900">{product.name}</h1>
                <div className="mt-1.5 flex items-center gap-2">
                  <Badge variant="outline">HS {product.hsCode}</Badge>
                  {product.active ? (
                    <Badge variant="teal">Active</Badge>
                  ) : (
                    <Badge variant="outline">Inactive</Badge>
                  )}
                </div>
              </div>
            </div>
            {canManage && (
              <div className="flex gap-2">
                <Button variant="outline" onClick={() => setEditing(true)}>
                  <Pencil /> Edit
                </Button>
                <Button variant="ghost" onClick={() => setDeleting(true)}>
                  <Trash2 className="text-destructive" /> Delete
                </Button>
              </div>
            )}
          </div>

          <Card>
            <CardContent className="p-6">
              <dl className="grid gap-5 sm:grid-cols-2 lg:grid-cols-4">
                <Field label="Unit price" value={formatMoney(product.unitPrice, product.currency)} />
                <Field label="Unit" value={product.unit} />
                <Field label="Min order qty" value={product.minOrderQty} />
                <Field label="Currency" value={product.currency} />
                <Field label="Listed" value={formatDate(product.createdAt)} />
                <Field label="Updated" value={formatDate(product.updatedAt)} />
              </dl>
              {product.description && (
                <div className="mt-6">
                  <dt className="text-sm text-muted-foreground">Description</dt>
                  <p className="mt-1 leading-relaxed text-navy-800">{product.description}</p>
                </div>
              )}
            </CardContent>
          </Card>
        </>
      )}

      {editing && (
        <ProductFormDialog
          product={product}
          onClose={() => setEditing(false)}
          onSaved={() => {
            qc.invalidateQueries({ queryKey: ["product", productId] });
            qc.invalidateQueries({ queryKey: ["products"] });
            setEditing(false);
          }}
        />
      )}
      {deleting && (
        <ConfirmDialog
          title="Delete product"
          description={`This removes “${product?.name}” from the catalog.`}
          confirmLabel="Delete"
          destructive
          pending={deleteMutation.isPending}
          onConfirm={() => deleteMutation.mutate()}
          onClose={() => setDeleting(false)}
        />
      )}
    </div>
  );
}
