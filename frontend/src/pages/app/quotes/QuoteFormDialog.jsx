import { useEffect } from "react";
import { useForm } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import { z } from "zod";
import { useAuth } from "@/auth/AuthContext";
import { useApiQuery, useApiMutation } from "@/hooks/useApi";
import { getRfqMatches } from "@/api/sourcing";
import { submitQuote } from "@/api/quotation";
import { INCOTERMS } from "@/data/enums";
import { FormField } from "@/components/common/FormField";
import { Button } from "@/components/ui/button";
import { Input, Select, Textarea } from "@/components/ui/input";
import { Dialog } from "@/components/ui/dialog";

const schema = z.object({
  productId: z.string().uuid("Pick a product to quote"),
  productName: z.string().optional(),
  quantity: z.coerce.number().int().positive("Must be greater than 0"),
  unit: z.string().optional(),
  unitPrice: z.coerce.number().positive("Enter a unit price"),
  currency: z.string().min(3).max(3),
  incoterm: z.string().optional(),
  validUntil: z.string().optional(),
  notes: z.string().optional(),
});

/**
 * Seller submits a quote against a buyer's RFQ. The product is chosen from the
 * catalog products that match the RFQ's HS code (cross-service lookup).
 */
export function QuoteFormDialog({ rfq, onClose, onSaved }) {
  const { user } = useAuth();
  const matchesQuery = useApiQuery(["rfq-matches", rfq.id], () => getRfqMatches(rfq.id));
  const matches = matchesQuery.data?.matches || [];

  const {
    register,
    handleSubmit,
    setValue,
    watch,
    formState: { errors },
  } = useForm({
    resolver: zodResolver(schema),
    defaultValues: {
      quantity: rfq.quantity || 1,
      unit: rfq.unit || "",
      currency: rfq.currency || "USD",
    },
  });

  const selectedId = watch("productId");

  // Prefill commercial fields from the chosen product.
  useEffect(() => {
    const p = matches.find((m) => m.id === selectedId);
    if (!p) return;
    setValue("productName", p.name);
    if (p.unit) setValue("unit", p.unit);
    if (p.unitPrice != null) setValue("unitPrice", p.unitPrice);
    if (p.currency) setValue("currency", p.currency);
  }, [selectedId, matches, setValue]);

  const mutation = useApiMutation(
    (values) =>
      submitQuote({
        ...values,
        rfqId: rfq.id,
        buyerCompanyId: rfq.buyerCompanyId,
        sellerCompanyId: user?.companyId,
        validUntil: values.validUntil ? new Date(values.validUntil).toISOString() : undefined,
      }),
    { successMessage: "Quote submitted", onSuccess: onSaved }
  );

  return (
    <Dialog
      open
      onClose={onClose}
      title="Submit a quote"
      description={`Respond to “${rfq.title}” with your commercial terms.`}
      footer={
        <>
          <Button variant="outline" onClick={onClose}>Cancel</Button>
          <Button
            variant="accent"
            onClick={handleSubmit((v) => mutation.mutate(v))}
            disabled={mutation.isPending}
          >
            {mutation.isPending ? "Submitting…" : "Submit quote"}
          </Button>
        </>
      }
    >
      <form className="space-y-4" onSubmit={handleSubmit((v) => mutation.mutate(v))}>
        <FormField
          label="Product"
          error={errors.productId?.message}
          required
          hint={
            matchesQuery.isLoading
              ? "Loading matching products…"
              : matches.length === 0
              ? `No catalog products match HS code ${rfq.hsCode}. List one in your catalog first.`
              : undefined
          }
        >
          <Select {...register("productId")} disabled={matches.length === 0}>
            <option value="">Select a product…</option>
            {matches.map((m) => (
              <option key={m.id} value={m.id}>
                {m.name} — HS {m.hsCode}
              </option>
            ))}
          </Select>
        </FormField>
        <div className="grid gap-4 sm:grid-cols-3">
          <FormField label="Quantity" error={errors.quantity?.message} required>
            <Input type="number" {...register("quantity")} />
          </FormField>
          <FormField label="Unit" error={errors.unit?.message}>
            <Input placeholder="MT" {...register("unit")} />
          </FormField>
          <FormField label="Incoterm" error={errors.incoterm?.message}>
            <Select {...register("incoterm")}>
              <option value="">—</option>
              {INCOTERMS.map((i) => (
                <option key={i} value={i}>{i}</option>
              ))}
            </Select>
          </FormField>
        </div>
        <div className="grid gap-4 sm:grid-cols-3">
          <FormField label="Unit price" error={errors.unitPrice?.message} required>
            <Input type="number" step="0.01" {...register("unitPrice")} />
          </FormField>
          <FormField label="Currency" error={errors.currency?.message} required>
            <Input maxLength={3} {...register("currency")} />
          </FormField>
          <FormField label="Valid until" error={errors.validUntil?.message}>
            <Input type="date" {...register("validUntil")} />
          </FormField>
        </div>
        <FormField label="Notes" error={errors.notes?.message}>
          <Textarea rows={2} placeholder="Lead time, payment terms, etc." {...register("notes")} />
        </FormField>
      </form>
    </Dialog>
  );
}
