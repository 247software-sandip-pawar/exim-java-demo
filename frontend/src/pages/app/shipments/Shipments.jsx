import { useState } from "react";
import { useNavigate } from "react-router-dom";
import { useForm } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import { z } from "zod";
import { Ship, Plus } from "lucide-react";
import { useAuth } from "@/auth/AuthContext";
import { useApiQuery, useApiMutation } from "@/hooks/useApi";
import { listShipments, createShipment, listPartners } from "@/api/logistics";
import { listOrders } from "@/api/orders";
import { SHIPMENT_STATUSES, TRANSPORT_MODES, INCOTERMS } from "@/data/enums";
import { formatDate } from "@/lib/format";
import { PageHeader } from "@/components/common/PageHeader";
import { DataTable } from "@/components/common/DataTable";
import { Pagination } from "@/components/common/Pagination";
import { StatusBadge } from "@/components/common/StatusBadge";
import { FormField } from "@/components/common/FormField";
import { Button } from "@/components/ui/button";
import { Input, Select } from "@/components/ui/input";
import { Dialog } from "@/components/ui/dialog";

const PAGE_SIZE = 10;

export default function Shipments() {
  const { user } = useAuth();
  const navigate = useNavigate();
  const [page, setPage] = useState(0);
  const [status, setStatus] = useState("");
  const [mineOnly, setMineOnly] = useState(false);
  const [booking, setBooking] = useState(false);

  const params = { page, size: PAGE_SIZE };
  if (status) params.status = status;

  const query = useApiQuery(["shipments", page, status], () => listShipments(params));
  const data = query.data;
  const rows = (data?.items || []).filter(
    (s) =>
      !mineOnly ||
      s.buyerCompanyId === user?.companyId ||
      s.sellerCompanyId === user?.companyId
  );

  const columns = [
    {
      key: "trackingNumber",
      header: "Tracking #",
      render: (s) => <span className="font-mono text-xs text-navy-700">{s.trackingNumber || s.id?.slice(0, 8)}</span>,
    },
    { key: "mode", header: "Mode", render: (s) => s.mode || "—" },
    {
      key: "route",
      header: "Route",
      render: (s) => `${s.origin || "?"} → ${s.destination || "?"}`,
    },
    { key: "eta", header: "ETA", render: (s) => formatDate(s.estimatedDelivery) },
    { key: "status", header: "Status", render: (s) => <StatusBadge status={s.status} /> },
  ];

  return (
    <div className="space-y-6">
      <PageHeader
        title="Shipments"
        description="Book and track shipments against your orders."
        actions={
          <Button variant="accent" onClick={() => setBooking(true)}>
            <Plus /> Book shipment
          </Button>
        }
      />

      <div className="flex flex-wrap items-center gap-2">
        <Button
          variant={mineOnly ? "default" : "outline"}
          size="sm"
          onClick={() => setMineOnly((v) => !v)}
        >
          {mineOnly ? "Showing my shipments" : "My shipments only"}
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
          {SHIPMENT_STATUSES.map((s) => (
            <option key={s} value={s}>{s.replace(/_/g, " ")}</option>
          ))}
        </Select>
      </div>

      <DataTable
        columns={columns}
        rows={rows}
        isLoading={query.isLoading}
        error={query.error}
        onRetry={query.refetch}
        onRowClick={(s) => navigate(`/app/shipments/${s.id}`)}
        empty={{
          icon: Ship,
          title: "No shipments yet",
          description: "Book a shipment for one of your orders.",
        }}
      />
      {!mineOnly && <Pagination page={page} totalPages={data?.totalPages} onChange={setPage} />}

      {booking && (
        <BookShipmentDialog
          onClose={() => setBooking(false)}
          onSaved={(s) => {
            query.refetch();
            setBooking(false);
            if (s?.id) navigate(`/app/shipments/${s.id}`);
          }}
        />
      )}
    </div>
  );
}

const schema = z.object({
  orderId: z.string().uuid("Pick an order"),
  partnerId: z.string().uuid("Pick a logistics partner"),
  mode: z.enum(TRANSPORT_MODES),
  origin: z.string().min(2, "Origin is required"),
  destination: z.string().min(2, "Destination is required"),
  incoterm: z.string().optional(),
  estimatedDelivery: z.string().optional(),
});

function BookShipmentDialog({ onClose, onSaved }) {
  const { user } = useAuth();
  const ordersQuery = useApiQuery(["orders", "picker"], () => listOrders({ size: 100 }));
  const partnersQuery = useApiQuery(["partners"], () => listPartners({ size: 100 }));
  const orders = (ordersQuery.data?.items || []).filter(
    (o) => o.buyerCompanyId === user?.companyId || o.sellerCompanyId === user?.companyId
  );
  const partners = partnersQuery.data?.items || [];

  const {
    register,
    handleSubmit,
    formState: { errors },
  } = useForm({ resolver: zodResolver(schema), defaultValues: { mode: "SEA" } });

  const mutation = useApiMutation(
    (values) =>
      createShipment({
        ...values,
        estimatedDelivery: values.estimatedDelivery
          ? new Date(values.estimatedDelivery).toISOString()
          : undefined,
      }),
    { successMessage: "Shipment booked", onSuccess: onSaved }
  );

  return (
    <Dialog
      open
      onClose={onClose}
      title="Book a shipment"
      description="Choose an order and a logistics partner to move the goods."
      footer={
        <>
          <Button variant="outline" onClick={onClose}>Cancel</Button>
          <Button
            variant="accent"
            onClick={handleSubmit((v) => mutation.mutate(v))}
            disabled={mutation.isPending}
          >
            {mutation.isPending ? "Booking…" : "Book shipment"}
          </Button>
        </>
      }
    >
      <form className="space-y-4" onSubmit={handleSubmit((v) => mutation.mutate(v))}>
        <FormField
          label="Order"
          error={errors.orderId?.message}
          required
          hint={ordersQuery.isLoading ? "Loading orders…" : orders.length === 0 ? "No orders to ship yet." : undefined}
        >
          <Select {...register("orderId")} disabled={orders.length === 0}>
            <option value="">Select an order…</option>
            {orders.map((o) => (
              <option key={o.id} value={o.id}>
                {o.id.slice(0, 8)} — {o.items?.[0]?.description || "order"} ({o.status})
              </option>
            ))}
          </Select>
        </FormField>
        <div className="grid gap-4 sm:grid-cols-2">
          <FormField
            label="Partner"
            error={errors.partnerId?.message}
            required
            hint={partnersQuery.isLoading ? "Loading partners…" : undefined}
          >
            <Select {...register("partnerId")}>
              <option value="">Select a partner…</option>
              {partners.map((p) => (
                <option key={p.id} value={p.id}>{p.name} ({p.mode})</option>
              ))}
            </Select>
          </FormField>
          <FormField label="Mode" error={errors.mode?.message} required>
            <Select {...register("mode")}>
              {TRANSPORT_MODES.map((m) => (
                <option key={m} value={m}>{m}</option>
              ))}
            </Select>
          </FormField>
        </div>
        <div className="grid gap-4 sm:grid-cols-2">
          <FormField label="Origin" error={errors.origin?.message} required>
            <Input placeholder="Mumbai, IN" {...register("origin")} />
          </FormField>
          <FormField label="Destination" error={errors.destination?.message} required>
            <Input placeholder="Rotterdam, NL" {...register("destination")} />
          </FormField>
        </div>
        <div className="grid gap-4 sm:grid-cols-2">
          <FormField label="Incoterm" error={errors.incoterm?.message}>
            <Select {...register("incoterm")}>
              <option value="">—</option>
              {INCOTERMS.map((i) => (
                <option key={i} value={i}>{i}</option>
              ))}
            </Select>
          </FormField>
          <FormField label="Estimated delivery" error={errors.estimatedDelivery?.message}>
            <Input type="date" {...register("estimatedDelivery")} />
          </FormField>
        </div>
      </form>
    </Dialog>
  );
}
