import { useState } from "react";
import { useForm } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import { z } from "zod";
import { useQueryClient } from "@tanstack/react-query";
import { UserCog, Plus, ShieldOff, ShieldCheck } from "lucide-react";
import { useApiQuery, useApiMutation } from "@/hooks/useApi";
import { listStaff, createStaff, setStaffActive } from "@/api/staff";
import { labelize } from "@/data/enums";
import { formatDate } from "@/lib/format";
import { PageHeader } from "@/components/common/PageHeader";
import { DataTable } from "@/components/common/DataTable";
import { Pagination } from "@/components/common/Pagination";
import { StatusBadge } from "@/components/common/StatusBadge";
import { FormField } from "@/components/common/FormField";
import { Button } from "@/components/ui/button";
import { Badge } from "@/components/ui/badge";
import { Input, Select } from "@/components/ui/input";
import { Dialog } from "@/components/ui/dialog";

const PAGE_SIZE = 10;
const STAFF_ROLES = ["PLATFORM_ADMIN", "SUPPORT"];

export default function Staff() {
  const qc = useQueryClient();
  const [page, setPage] = useState(0);
  const [creating, setCreating] = useState(false);

  const query = useApiQuery(["staff", page], () => listStaff({ page, size: PAGE_SIZE }));
  const data = query.data;

  const toggle = useApiMutation(({ id, active }) => setStaffActive(id, active), {
    successMessage: "Staff updated",
    onSuccess: () => qc.invalidateQueries({ queryKey: ["staff"] }),
  });

  const columns = [
    { key: "name", header: "Name", render: (s) => <span className="font-medium">{s.name}</span> },
    { key: "email", header: "Email" },
    { key: "role", header: "Role", render: (s) => <Badge variant="outline">{labelize(s.role)}</Badge> },
    {
      key: "active",
      header: "Status",
      render: (s) => <StatusBadge status={s.active ? "ACTIVE" : "DISABLED"} />,
    },
    { key: "createdAt", header: "Added", render: (s) => formatDate(s.createdAt) },
    {
      key: "actions",
      header: "",
      align: "right",
      render: (s) => (
        <Button
          variant="ghost"
          size="sm"
          disabled={toggle.isPending}
          onClick={() => toggle.mutate({ id: s.id, active: !s.active })}
        >
          {s.active ? <><ShieldOff className="text-destructive" /> Deactivate</> : <><ShieldCheck className="text-teal-600" /> Activate</>}
        </Button>
      ),
    },
  ];

  return (
    <div className="space-y-6">
      <PageHeader
        title="Platform staff"
        description="Administrators and support — provisioned here, independent of company sign-up."
        actions={
          <Button variant="accent" onClick={() => setCreating(true)}>
            <Plus /> New staff
          </Button>
        }
      />
      <DataTable
        columns={columns}
        rows={data?.items}
        isLoading={query.isLoading}
        error={query.error}
        onRetry={query.refetch}
        rowKey="id"
        empty={{ icon: UserCog, title: "No staff yet", description: "Add a platform admin or support agent." }}
      />
      <Pagination page={page} totalPages={data?.totalPages} onChange={setPage} />

      {creating && (
        <StaffFormDialog
          onClose={() => setCreating(false)}
          onSaved={() => {
            query.refetch();
            setCreating(false);
          }}
        />
      )}
    </div>
  );
}

const schema = z.object({
  name: z.string().min(2, "Name is required"),
  email: z.string().email("Valid email required"),
  password: z.string().min(8, "Min 8 characters"),
  role: z.enum(STAFF_ROLES, { message: "Select a role" }),
});

function StaffFormDialog({ onClose, onSaved }) {
  const {
    register,
    handleSubmit,
    formState: { errors },
  } = useForm({ resolver: zodResolver(schema), defaultValues: { role: "SUPPORT" } });

  const mutation = useApiMutation((values) => createStaff(values), {
    successMessage: "Staff account created",
    onSuccess: onSaved,
  });

  return (
    <Dialog
      open
      onClose={onClose}
      title="New staff account"
      description="Creates a PLATFORM_ADMIN or SUPPORT account with no company."
      footer={
        <>
          <Button variant="outline" onClick={onClose}>Cancel</Button>
          <Button variant="accent" onClick={handleSubmit((v) => mutation.mutate(v))} disabled={mutation.isPending}>
            {mutation.isPending ? "Creating…" : "Create"}
          </Button>
        </>
      }
    >
      <form className="space-y-4" onSubmit={handleSubmit((v) => mutation.mutate(v))}>
        <FormField label="Name" error={errors.name?.message} required>
          <Input {...register("name")} />
        </FormField>
        <FormField label="Email" error={errors.email?.message} required>
          <Input type="email" {...register("email")} />
        </FormField>
        <FormField label="Password" error={errors.password?.message} required>
          <Input type="password" {...register("password")} />
        </FormField>
        <FormField label="Role" error={errors.role?.message} required>
          <Select {...register("role")}>
            {STAFF_ROLES.map((r) => (
              <option key={r} value={r}>{labelize(r)}</option>
            ))}
          </Select>
        </FormField>
      </form>
    </Dialog>
  );
}
