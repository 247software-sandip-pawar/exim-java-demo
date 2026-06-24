import { useState } from "react";
import { useParams, Link } from "react-router-dom";
import { useQueryClient } from "@tanstack/react-query";
import { ArrowLeft, Ship, Plus, MapPin } from "lucide-react";
import { useApiQuery, useApiMutation } from "@/hooks/useApi";
import { getShipment, addTrackingEvent } from "@/api/logistics";
import { SHIPMENT_STATUS_NEXT } from "@/data/enums";
import { formatDate, formatDateTime } from "@/lib/format";
import { LoadingState } from "@/components/common/LoadingState";
import { ErrorState } from "@/components/common/ErrorState";
import { EmptyState } from "@/components/common/EmptyState";
import { PageHeader } from "@/components/common/PageHeader";
import { StatusBadge } from "@/components/common/StatusBadge";
import { FormField } from "@/components/common/FormField";
import { Card, CardContent } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { Input, Select } from "@/components/ui/input";
import { Dialog } from "@/components/ui/dialog";

function Field({ label, value }) {
  return (
    <div>
      <dt className="text-sm text-muted-foreground">{label}</dt>
      <dd className="mt-0.5 font-medium text-navy-900">{value ?? "—"}</dd>
    </div>
  );
}

export default function ShipmentDetail() {
  const { shipmentId } = useParams();
  const [adding, setAdding] = useState(false);

  const { data: shipment, isLoading, error, refetch } = useApiQuery(
    ["shipment", shipmentId],
    () => getShipment(shipmentId)
  );

  const allowedNext = shipment ? SHIPMENT_STATUS_NEXT[shipment.status] || [] : [];
  // Newest first for the timeline.
  const events = [...(shipment?.events || [])].sort(
    (a, b) => new Date(b.occurredAt) - new Date(a.occurredAt)
  );

  return (
    <div className="space-y-6">
      <Link
        to="/app/shipments"
        className="inline-flex items-center gap-1.5 text-sm font-medium text-muted-foreground hover:text-navy-800"
      >
        <ArrowLeft className="size-4" /> Back to shipments
      </Link>

      {isLoading ? (
        <LoadingState />
      ) : error ? (
        <ErrorState error={error} onRetry={refetch} />
      ) : (
        <>
          <PageHeader
            title={
              <span className="flex items-center gap-3">
                <span className="grid size-10 place-items-center rounded-xl bg-secondary text-primary">
                  <Ship className="size-5" />
                </span>
                {shipment.trackingNumber || `Shipment ${shipment.id?.slice(0, 8)}`}
              </span>
            }
            actions={
              allowedNext.length > 0 && (
                <Button variant="accent" onClick={() => setAdding(true)}>
                  <Plus /> Add tracking event
                </Button>
              )
            }
          />

          <div className="flex items-center gap-2">
            <StatusBadge status={shipment.status} />
            <Link
              to={`/app/orders/${shipment.orderId}`}
              className="text-sm text-muted-foreground hover:text-navy-800 hover:underline"
            >
              for order {shipment.orderId?.slice(0, 8)}
            </Link>
          </div>

          <Card>
            <CardContent className="p-6">
              <dl className="grid gap-5 sm:grid-cols-2 lg:grid-cols-4">
                <Field label="Mode" value={shipment.mode} />
                <Field label="Origin" value={shipment.origin} />
                <Field label="Destination" value={shipment.destination} />
                <Field label="Incoterm" value={shipment.incoterm || "—"} />
                <Field label="Estimated delivery" value={formatDate(shipment.estimatedDelivery)} />
                <Field label="Booked" value={formatDateTime(shipment.createdAt)} />
              </dl>
            </CardContent>
          </Card>

          {/* Tracking timeline */}
          <div>
            <h2 className="mb-3 text-lg font-semibold">Tracking timeline</h2>
            {events.length === 0 ? (
              <EmptyState
                icon={MapPin}
                title="No tracking events yet"
                description="Add an event to advance the shipment."
              />
            ) : (
              <Card>
                <CardContent className="p-6">
                  <ol className="relative space-y-6 border-l border-border pl-6">
                    {events.map((e, i) => (
                      <li key={i} className="relative">
                        <span className="absolute -left-[1.65rem] top-1 grid size-3 place-items-center rounded-full bg-gold-500 ring-4 ring-card" />
                        <div className="flex flex-wrap items-center gap-2">
                          <StatusBadge status={e.status} />
                          {e.location && (
                            <span className="inline-flex items-center gap-1 text-sm text-navy-800">
                              <MapPin className="size-3.5 text-muted-foreground" /> {e.location}
                            </span>
                          )}
                          <span className="text-xs text-muted-foreground">
                            {formatDateTime(e.occurredAt)}
                          </span>
                        </div>
                        {e.note && <p className="mt-1 text-sm text-navy-700">{e.note}</p>}
                      </li>
                    ))}
                  </ol>
                </CardContent>
              </Card>
            )}
          </div>
        </>
      )}

      {adding && (
        <AddEventDialog
          shipmentId={shipmentId}
          allowedNext={allowedNext}
          onClose={() => setAdding(false)}
        />
      )}
    </div>
  );
}

function AddEventDialog({ shipmentId, allowedNext, onClose }) {
  const qc = useQueryClient();
  const [status, setStatus] = useState(allowedNext[0] || "");
  const [location, setLocation] = useState("");
  const [note, setNote] = useState("");

  const mutation = useApiMutation(
    () => addTrackingEvent(shipmentId, { status, location: location || undefined, note: note || undefined }),
    {
      successMessage: "Tracking event added",
      onSuccess: () => {
        qc.invalidateQueries({ queryKey: ["shipment", shipmentId] });
        qc.invalidateQueries({ queryKey: ["shipments"] });
        onClose();
      },
    }
  );

  return (
    <Dialog
      open
      onClose={onClose}
      title="Add tracking event"
      description="Advance the shipment and record where it is."
      footer={
        <>
          <Button variant="outline" onClick={onClose}>Cancel</Button>
          <Button variant="accent" disabled={!status || mutation.isPending} onClick={() => mutation.mutate()}>
            {mutation.isPending ? "Adding…" : "Add event"}
          </Button>
        </>
      }
    >
      <div className="space-y-4">
        <FormField label="New status" required>
          <Select value={status} onChange={(e) => setStatus(e.target.value)}>
            {allowedNext.map((s) => (
              <option key={s} value={s}>{s.replace(/_/g, " ")}</option>
            ))}
          </Select>
        </FormField>
        <FormField label="Location">
          <Input placeholder="e.g. Suez Canal" value={location} onChange={(e) => setLocation(e.target.value)} />
        </FormField>
        <FormField label="Note">
          <Input placeholder="Optional detail" value={note} onChange={(e) => setNote(e.target.value)} />
        </FormField>
      </div>
    </Dialog>
  );
}
