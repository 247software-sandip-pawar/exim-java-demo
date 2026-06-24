import { useState } from "react";
import { useParams, Link } from "react-router-dom";
import { useQueryClient } from "@tanstack/react-query";
import { ArrowLeft, ShoppingCart, FileText, Plus, ExternalLink } from "lucide-react";
import { useApiQuery, useApiMutation } from "@/hooks/useApi";
import { getOrder, updateOrderStatus } from "@/api/orders";
import { listDocuments, generateDocument } from "@/api/documents";
import { ORDER_STATUS_NEXT, DOCUMENT_TYPES, DOCUMENT_TYPE_LABELS } from "@/data/enums";
import { formatMoney, formatDate, formatDateTime } from "@/lib/format";
import { LoadingState } from "@/components/common/LoadingState";
import { ErrorState } from "@/components/common/ErrorState";
import { PageHeader } from "@/components/common/PageHeader";
import { DataTable } from "@/components/common/DataTable";
import { StatusBadge } from "@/components/common/StatusBadge";
import { FormField } from "@/components/common/FormField";
import { Card, CardContent } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { Select } from "@/components/ui/input";
import { Dialog } from "@/components/ui/dialog";

function Field({ label, value }) {
  return (
    <div>
      <dt className="text-sm text-muted-foreground">{label}</dt>
      <dd className="mt-0.5 font-medium text-navy-900">{value ?? "—"}</dd>
    </div>
  );
}

export default function OrderDetail() {
  const { orderId } = useParams();
  const qc = useQueryClient();
  const [nextStatus, setNextStatus] = useState("");
  const [generating, setGenerating] = useState(false);

  const orderQuery = useApiQuery(["order", orderId], () => getOrder(orderId));
  const docsQuery = useApiQuery(["documents", orderId], () => listDocuments({ orderId, size: 50 }));

  const order = orderQuery.data;
  const docs = docsQuery.data?.items || [];
  const allowedNext = order ? ORDER_STATUS_NEXT[order.status] || [] : [];

  const statusMutation = useApiMutation((status) => updateOrderStatus(orderId, status), {
    successMessage: "Order status updated",
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: ["order", orderId] });
      qc.invalidateQueries({ queryKey: ["orders"] });
      setNextStatus("");
    },
  });

  const itemColumns = [
    {
      key: "description",
      header: "Item",
      render: (i) => <span className="font-medium">{i.description || "—"}</span>,
    },
    { key: "quantity", header: "Qty", render: (i) => `${i.quantity} ${i.unit || ""}`.trim() },
    { key: "unitPrice", header: "Unit price", render: (i) => formatMoney(i.unitPrice, order.currency) },
    {
      key: "lineTotal",
      header: "Line total",
      align: "right",
      render: (i) => formatMoney(i.lineTotal, order.currency),
    },
  ];

  const docColumns = [
    {
      key: "type",
      header: "Document",
      render: (d) => (
        <span className="font-medium">{DOCUMENT_TYPE_LABELS[d.type] || d.type}</span>
      ),
    },
    { key: "documentNumber", header: "Number", render: (d) => d.documentNumber || "—" },
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
      <Link
        to="/app/orders"
        className="inline-flex items-center gap-1.5 text-sm font-medium text-muted-foreground hover:text-navy-800"
      >
        <ArrowLeft className="size-4" /> Back to orders
      </Link>

      {orderQuery.isLoading ? (
        <LoadingState />
      ) : orderQuery.error ? (
        <ErrorState error={orderQuery.error} onRetry={orderQuery.refetch} />
      ) : (
        <>
          <PageHeader
            title={
              <span className="flex items-center gap-3">
                <span className="grid size-10 place-items-center rounded-xl bg-secondary text-primary">
                  <ShoppingCart className="size-5" />
                </span>
                Order {order.id?.slice(0, 8)}
              </span>
            }
            actions={
              allowedNext.length > 0 && (
                <div className="flex items-center gap-2">
                  <Select
                    className="h-10 w-auto"
                    value={nextStatus}
                    onChange={(e) => setNextStatus(e.target.value)}
                  >
                    <option value="">Advance status…</option>
                    {allowedNext.map((s) => (
                      <option key={s} value={s}>{s.replace(/_/g, " ")}</option>
                    ))}
                  </Select>
                  <Button
                    variant="accent"
                    disabled={!nextStatus || statusMutation.isPending}
                    onClick={() => statusMutation.mutate(nextStatus)}
                  >
                    Update
                  </Button>
                </div>
              )
            }
          />

          <div className="flex items-center gap-2">
            <StatusBadge status={order.status} />
            <Link
              to={`/app/quotes/${order.quoteId}`}
              className="text-sm text-muted-foreground hover:text-navy-800 hover:underline"
            >
              from accepted quote
            </Link>
          </div>

          <Card>
            <CardContent className="p-6">
              <dl className="grid gap-5 sm:grid-cols-2 lg:grid-cols-4">
                <Field label="Total amount" value={formatMoney(order.totalAmount, order.currency)} />
                <Field label="Currency" value={order.currency} />
                <Field label="Created" value={formatDateTime(order.createdAt)} />
                <Field label="Updated" value={formatDateTime(order.updatedAt)} />
              </dl>
            </CardContent>
          </Card>

          <div>
            <h2 className="mb-3 text-lg font-semibold">Items</h2>
            <DataTable
              columns={itemColumns}
              rows={order.items}
              rowKey={(i) => i.productId || i.description}
              empty={{ icon: ShoppingCart, title: "No line items" }}
            />
          </div>

          {/* Trade documents for this order */}
          <div>
            <div className="mb-3 flex items-center justify-between gap-2">
              <div className="flex items-center gap-2">
                <FileText className="size-5 text-primary" />
                <h2 className="text-lg font-semibold">
                  Documents
                  <span className="text-muted-foreground"> ({docs.length})</span>
                </h2>
              </div>
              <Button variant="accent" size="sm" onClick={() => setGenerating(true)}>
                <Plus /> Generate document
              </Button>
            </div>
            <DataTable
              columns={docColumns}
              rows={docs}
              isLoading={docsQuery.isLoading}
              error={docsQuery.error}
              onRetry={docsQuery.refetch}
              rowKey="id"
              empty={{
                icon: FileText,
                title: "No documents yet",
                description: "Generate a proforma or commercial invoice for this order.",
              }}
            />
          </div>
        </>
      )}

      {generating && (
        <GenerateDocumentDialog
          orderId={orderId}
          onClose={() => setGenerating(false)}
          onSaved={() => {
            qc.invalidateQueries({ queryKey: ["documents", orderId] });
            setGenerating(false);
          }}
        />
      )}
    </div>
  );
}

function GenerateDocumentDialog({ orderId, onClose, onSaved }) {
  const [type, setType] = useState(DOCUMENT_TYPES[0]);
  const mutation = useApiMutation(() => generateDocument({ orderId, type }), {
    successMessage: "Document generated",
    onSuccess: onSaved,
  });

  return (
    <Dialog
      open
      onClose={onClose}
      title="Generate document"
      description="Create a trade document from this order's snapshot."
      footer={
        <>
          <Button variant="outline" onClick={onClose}>Cancel</Button>
          <Button variant="accent" disabled={mutation.isPending} onClick={() => mutation.mutate()}>
            {mutation.isPending ? "Generating…" : "Generate"}
          </Button>
        </>
      }
    >
      <FormField label="Document type" required>
        <Select value={type} onChange={(e) => setType(e.target.value)}>
          {DOCUMENT_TYPES.map((t) => (
            <option key={t} value={t}>{DOCUMENT_TYPE_LABELS[t]}</option>
          ))}
        </Select>
      </FormField>
    </Dialog>
  );
}
