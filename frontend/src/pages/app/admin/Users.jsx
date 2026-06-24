import { useState } from "react";
import { useForm } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import { z } from "zod";
import { useQueryClient } from "@tanstack/react-query";
import { Plus, Pencil, Trash2, Users as UsersIcon } from "lucide-react";
import { useAuth } from "@/auth/AuthContext";
import { useApiQuery, useApiMutation } from "@/hooks/useApi";
import {
  listUsers,
  createUser,
  updateUser,
  deleteUser,
  listCompanies,
} from "@/api/identity";
import { ROLES, labelize } from "@/data/enums";
import { PageHeader } from "@/components/common/PageHeader";
import { DataTable } from "@/components/common/DataTable";
import { Pagination } from "@/components/common/Pagination";
import { FormField } from "@/components/common/FormField";
import { Button } from "@/components/ui/button";
import { Badge } from "@/components/ui/badge";
import { Input, Select } from "@/components/ui/input";
import { Dialog } from "@/components/ui/dialog";

const PAGE_SIZE = 10;

const baseShape = {
  name: z.string().min(2, "Name is required"),
  phone: z.string().optional(),
  role: z.enum(ROLES, { message: "Select a role" }),
  companyId: z.string().optional(),
};
const createSchema = z.object({
  ...baseShape,
  email: z.string().email("Valid email required"),
  password: z.string().min(8, "Min 8 characters"),
});
const editSchema = z.object(baseShape);

export default function Users() {
  const { hasRole } = useAuth();
  const canManage = hasRole("PLATFORM_ADMIN");
  const qc = useQueryClient();
  const [page, setPage] = useState(0);
  const [editing, setEditing] = useState(null); // null | {} (new) | user (edit)
  const [deleting, setDeleting] = useState(null);

  const usersQuery = useApiQuery(["users", page], () =>
    listUsers({ page, size: PAGE_SIZE })
  );
  const companiesQuery = useApiQuery(["companies", "all"], () =>
    listCompanies({ page: 0, size: 200 })
  );
  const companies = companiesQuery.data?.items || [];

  const invalidate = () => qc.invalidateQueries({ queryKey: ["users"] });

  const columns = [
    { key: "name", header: "Name", render: (u) => <span className="font-medium">{u.name}</span> },
    { key: "email", header: "Email" },
    { key: "role", header: "Role", render: (u) => <Badge variant="outline">{labelize(u.role)}</Badge> },
    { key: "companyName", header: "Company", render: (u) => u.companyName || "—" },
    { key: "phone", header: "Phone", render: (u) => u.phone || "—" },
    ...(canManage
      ? [
          {
            key: "actions",
            header: "",
            align: "right",
            render: (u) => (
              <div className="flex justify-end gap-1">
                <Button variant="ghost" size="icon" onClick={() => setEditing(u)} aria-label="Edit">
                  <Pencil />
                </Button>
                <Button variant="ghost" size="icon" onClick={() => setDeleting(u)} aria-label="Delete">
                  <Trash2 className="text-destructive" />
                </Button>
              </div>
            ),
          },
        ]
      : []),
  ];

  const page_ = usersQuery.data;

  return (
    <div className="space-y-6">
      <PageHeader
        title="Users"
        description="Manage platform user accounts."
        actions={
          canManage && (
            <Button variant="accent" onClick={() => setEditing({})}>
              <Plus /> New user
            </Button>
          )
        }
      />

      <DataTable
        columns={columns}
        rows={page_?.items}
        isLoading={usersQuery.isLoading}
        error={usersQuery.error}
        onRetry={usersQuery.refetch}
        empty={{
          icon: UsersIcon,
          title: "No users yet",
          description: canManage ? "Create the first user to get started." : undefined,
        }}
      />
      <Pagination page={page} totalPages={page_?.totalPages} onChange={setPage} />

      {editing && (
        <UserFormDialog
          user={editing.id ? editing : null}
          companies={companies}
          onClose={() => setEditing(null)}
          onSaved={() => {
            invalidate();
            setEditing(null);
          }}
        />
      )}

      {deleting && (
        <DeleteUserDialog
          user={deleting}
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

function UserFormDialog({ user, companies, onClose, onSaved }) {
  const isEdit = Boolean(user);
  const {
    register,
    handleSubmit,
    formState: { errors },
  } = useForm({
    resolver: zodResolver(isEdit ? editSchema : createSchema),
    defaultValues: {
      name: user?.name || "",
      email: user?.email || "",
      phone: user?.phone || "",
      role: user?.role || "",
      companyId: user?.companyId || "",
    },
  });

  const mutation = useApiMutation(
    (values) => {
      const payload = { ...values, companyId: values.companyId || null };
      if (isEdit) {
        delete payload.email;
        delete payload.password;
        return updateUser(user.id, payload);
      }
      return createUser(payload);
    },
    {
      successMessage: isEdit ? "User updated" : "User created",
      onSuccess: onSaved,
    }
  );

  return (
    <Dialog
      open
      onClose={onClose}
      title={isEdit ? "Edit user" : "New user"}
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
        <FormField label="Name" error={errors.name?.message} required>
          <Input {...register("name")} />
        </FormField>
        {!isEdit && (
          <>
            <FormField label="Email" error={errors.email?.message} required>
              <Input type="email" {...register("email")} />
            </FormField>
            <FormField label="Password" error={errors.password?.message} required>
              <Input type="password" {...register("password")} />
            </FormField>
          </>
        )}
        <div className="grid gap-4 sm:grid-cols-2">
          <FormField label="Role" error={errors.role?.message} required>
            <Select {...register("role")} defaultValue={user?.role || ""}>
              <option value="" disabled>Select…</option>
              {ROLES.map((r) => (
                <option key={r} value={r}>{labelize(r)}</option>
              ))}
            </Select>
          </FormField>
          <FormField label="Phone" error={errors.phone?.message}>
            <Input {...register("phone")} />
          </FormField>
        </div>
        <FormField label="Company" hint="Leave blank for platform staff">
          <Select {...register("companyId")} defaultValue={user?.companyId || ""}>
            <option value="">— none —</option>
            {companies.map((c) => (
              <option key={c.id} value={c.id}>{c.name}</option>
            ))}
          </Select>
        </FormField>
      </form>
    </Dialog>
  );
}

function DeleteUserDialog({ user, onClose, onDeleted }) {
  const mutation = useApiMutation(() => deleteUser(user.id), {
    successMessage: "User deleted",
    onSuccess: onDeleted,
  });
  return (
    <Dialog
      open
      onClose={onClose}
      title="Delete user"
      description={`This permanently removes ${user.name}.`}
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
