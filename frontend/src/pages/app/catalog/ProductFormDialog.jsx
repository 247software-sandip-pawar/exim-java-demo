import { useForm } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import { z } from "zod";
import { useAuth } from "@/auth/AuthContext";
import { useApiMutation } from "@/hooks/useApi";
import { createProduct, updateProduct } from "@/api/catalog";
import { FormField } from "@/components/common/FormField";
import { Dialog } from "@/components/ui/dialog";
import { Button } from "@/components/ui/button";
import { Input, Textarea } from "@/components/ui/input";

const schema = z.object({
  name: z.string().min(2, "Name is required"),
  description: z.string().optional(),
  hsCode: z.string().min(2, "HS code is required"),
  unitPrice: z.coerce.number().positive("Must be greater than 0"),
  currency: z.string().min(3, "e.g. USD").max(3),
  unit: z.string().min(1, "e.g. MT, kg, pcs"),
  minOrderQty: z.coerce.number().int().min(1, "Min 1"),
  active: z.boolean().optional(),
});

export function ProductFormDialog({ product, onClose, onSaved }) {
  const { user } = useAuth();
  const isEdit = Boolean(product);
  const {
    register,
    handleSubmit,
    formState: { errors },
  } = useForm({
    resolver: zodResolver(schema),
    defaultValues: {
      name: product?.name || "",
      description: product?.description || "",
      hsCode: product?.hsCode || "",
      unitPrice: product?.unitPrice ?? "",
      currency: product?.currency || "USD",
      unit: product?.unit || "",
      minOrderQty: product?.minOrderQty ?? 1,
      active: product?.active ?? true,
    },
  });

  const mutation = useApiMutation(
    (values) => {
      const payload = { ...values, companyId: product?.companyId || user?.companyId };
      return isEdit ? updateProduct(product.id, payload) : createProduct(payload);
    },
    {
      successMessage: isEdit ? "Product updated" : "Product created",
      onSuccess: onSaved,
    }
  );

  return (
    <Dialog
      open
      onClose={onClose}
      title={isEdit ? "Edit product" : "New product"}
      footer={
        <>
          <Button variant="outline" onClick={onClose}>Cancel</Button>
          <Button
            variant="accent"
            onClick={handleSubmit((v) => mutation.mutate(v))}
            disabled={mutation.isPending}
          >
            {mutation.isPending ? "Saving…" : "Save"}
          </Button>
        </>
      }
    >
      <form className="space-y-4" onSubmit={handleSubmit((v) => mutation.mutate(v))}>
        <FormField label="Product name" error={errors.name?.message} required>
          <Input {...register("name")} />
        </FormField>
        <FormField label="Description" error={errors.description?.message}>
          <Textarea rows={3} {...register("description")} />
        </FormField>
        <div className="grid gap-4 sm:grid-cols-2">
          <FormField label="HS code" error={errors.hsCode?.message} required>
            <Input placeholder="1006.30" {...register("hsCode")} />
          </FormField>
          <FormField label="Unit" error={errors.unit?.message} required>
            <Input placeholder="MT" {...register("unit")} />
          </FormField>
        </div>
        <div className="grid gap-4 sm:grid-cols-3">
          <FormField label="Unit price" error={errors.unitPrice?.message} required>
            <Input type="number" step="0.01" {...register("unitPrice")} />
          </FormField>
          <FormField label="Currency" error={errors.currency?.message} required>
            <Input placeholder="USD" maxLength={3} {...register("currency")} />
          </FormField>
          <FormField label="Min order qty" error={errors.minOrderQty?.message} required>
            <Input type="number" {...register("minOrderQty")} />
          </FormField>
        </div>
        <label className="flex items-center gap-2.5 text-sm font-medium text-navy-800">
          <input type="checkbox" className="size-4 rounded border-input" {...register("active")} />
          Active (visible in catalog)
        </label>
      </form>
    </Dialog>
  );
}
