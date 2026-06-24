import { useState } from "react";
import { useForm } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import { z } from "zod";
import { useQueryClient } from "@tanstack/react-query";
import { Plus, Pencil, Trash2, Building2, Ban, CheckCircle2 } from "lucide-react";
import { useApiQuery, useApiMutation } from "@/hooks/useApi";
import {
  listCompanies,
  createCompany,
  updateCompany,
  deleteCompany,
  setCompanyActive,
} from "@/api/identity";
import { COMPANY_TYPES, labelize } from "@/data/enums";
import { PageHeader } from "@/components/common/PageHeader";
import { DataTable } from "@/components/common/DataTable";
import { Pagination } from "@/components/common/Pagination";
import { FormField } from "@/components/common/FormField";
import { StatusBadge } from "@/components/common/StatusBadge";
import { Button } from "@/components/ui/button";
import { Badge } from "@/components/ui/badge";
import { Input, Select } from "@/components/ui/input";
import { Dialog } from "@/components/ui/dialog";

const PAGE_SIZE = 10;

const schema = z.object({
  name: z.string().min(2, "Name is required"),
  type: z.enum(COMPANY_TYPES, { message: "Select a type" }),
  country: z.string().min(2, "Country is required"),
  iecCode: z.string().optional(),
  gstin: z.string().optional(),
  verified: z.boolean().optional(),
});

export default function Companies() {
  const qc = useQueryClient();
  const [page, setPage] = useState(0);
  const [editing, setEditing] = useState(null);
  const [deleting, setDeleting] = useState(null);

  const query = useApiQuery(["companies", page], () =>
    listCompanies({ page, size: PAGE_SIZE })
  );
  const invalidate = () => qc.invalidateQueries({ queryKey: ["companies"] });
  const data = query.data;

  const toggleActive = useApiMutation(({ id, active }) => setCompanyActive(id, active), {
    successMessage: "Company updated",
    onSuccess: invalidate,
  });

  const columns = [
    { key: "name", header: "Company", render: (c) => <span className="font-medium">{c.name}</span> },
    { key: "type", header: "Type", render: (c) => <Badge variant="outline">{labelize(c.type)}</Badge> },
    { key: "country", header: "Country" },
    { key: "verified", header: "KYC", render: (c) => <StatusBadge status={c.verified ? "VERIFIED" : "PENDING"} /> },
    { key: "active", header: "Status", render: (c) => <StatusBadge status={c.active ? "ACTIVE" : "SUSPENDED"} /> },
    {
      key: "actions",
      header: "",
      align: "right",
      render: (c) => (
        <div className="flex justify-end gap-1">
          <Button
            variant="ghost"
            size="icon"
            disabled={toggleActive.isPending}
            onClick={() => toggleActive.mutate({ id: c.id, active: !c.active })}
            aria-label={c.active ? "Suspend" : "Reinstate"}
            title={c.active ? "Suspend" : "Reinstate"}
          >
            {c.active ? <Ban className="text-destructive" /> : <CheckCircle2 className="text-teal-600" />}
          </Button>
          <Button variant="ghost" size="icon" onClick={() => setEditing(c)} aria-label="Edit">
            <Pencil />
          </Button>
          <Button variant="ghost" size="icon" onClick={() => setDeleting(c)} aria-label="Delete">
            <Trash2 className="text-destructive" />
          </Button>
        </div>
      ),
    },
  ];

  return (
    <div className="space-y-6">
      <PageHeader
        title="Companies"
        description="Manage registered companies on the platform."
        actions={
          <Button variant="accent" onClick={() => setEditing({})}>
            <Plus /> New company
          </Button>
        }
      />

      <DataTable
        columns={columns}
        rows={data?.items}
        isLoading={query.isLoading}
        error={query.error}
        onRetry={query.refetch}
        empty={{ icon: Building2, title: "No companies yet" }}
      />
      <Pagination page={page} totalPages={data?.totalPages} onChange={setPage} />

      {editing && (
        <CompanyFormDialog
          company={editing.id ? editing : null}
          onClose={() => setEditing(null)}
          onSaved={() => {
            invalidate();
            setEditing(null);
          }}
        />
      )}
      {deleting && (
        <DeleteCompanyDialog
          company={deleting}
          onClose={() => setDeleting(null)}
          onDeleted={() => {
            invalidate();
            setDeleting(null);
          }}
        />
      )}
    </div>
  );
}

function CompanyFormDialog({ company, onClose, onSaved }) {
  const isEdit = Boolean(company);
  const {
    register,
    handleSubmit,
    formState: { errors },
  } = useForm({
    resolver: zodResolver(schema),
    defaultValues: {
      name: company?.name || "",
      type: company?.type || "",
      country: company?.country || "",
      iecCode: company?.iecCode || "",
      gstin: company?.gstin || "",
      verified: company?.verified || false,
    },
  });

  const mutation = useApiMutation(
    (values) => (isEdit ? updateCompany(company.id, values) : createCompany(values)),
    {
      successMessage: isEdit ? "Company updated" : "Company created",
      onSuccess: onSaved,
    }
  );

  return (
    <Dialog
      open
      onClose={onClose}
      title={isEdit ? "Edit company" : "New company"}
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
        <FormField label="Company name" error={errors.name?.message} required>
          <Input {...register("name")} />
        </FormField>
        <div className="grid gap-4 sm:grid-cols-2">
          <FormField label="Type" error={errors.type?.message} required>
            <Select {...register("type")} defaultValue={company?.type || ""}>
              <option value="" disabled>Select…</option>
              {COMPANY_TYPES.map((t) => (
                <option key={t} value={t}>{labelize(t)}</option>
              ))}
            </Select>
          </FormField>
          <FormField label="Country" error={errors.country?.message} required>
            <Input {...register("country")} />
          </FormField>
        </div>
        <div className="grid gap-4 sm:grid-cols-2">
          <FormField label="IEC code" error={errors.iecCode?.message}>
            <Input {...register("iecCode")} />
          </FormField>
          <FormField label="GSTIN" error={errors.gstin?.message}>
            <Input {...register("gstin")} />
          </FormField>
        </div>
        <label className="flex items-center gap-2.5 text-sm font-medium text-navy-800">
          <input type="checkbox" className="size-4 rounded border-input" {...register("verified")} />
          Mark as verified
        </label>
      </form>
    </Dialog>
  );
}

function DeleteCompanyDialog({ company, onClose, onDeleted }) {
  const mutation = useApiMutation(() => deleteCompany(company.id), {
    successMessage: "Company deleted",
    onSuccess: onDeleted,
  });
  return (
    <Dialog
      open
      onClose={onClose}
      title="Delete company"
      description={`This permanently removes ${company.name}.`}
      footer={
        <>
          <Button variant="outline" onClick={onClose}>Cancel</Button>
          <Button
            className="bg-destructive text-destructive-foreground hover:bg-destructive/90"
            onClick={() => mutation.mutate()}
            disabled={mutation.isPending}
          >
            {mutation.isPending ? "Deleting…" : "Delete"}
          </Button>
        </>
      }
    >
      <p className="text-sm text-muted-foreground">
        Are you sure? This action cannot be undone.
      </p>
    </Dialog>
  );
}
