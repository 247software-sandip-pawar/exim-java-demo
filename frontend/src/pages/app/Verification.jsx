import { useState } from "react";
import { useForm } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import { z } from "zod";
import { useQueryClient } from "@tanstack/react-query";
import { Plus, BadgeCheck, FileText, ExternalLink } from "lucide-react";
import { useAuth } from "@/auth/AuthContext";
import { useApiQuery, useApiMutation } from "@/hooks/useApi";
import { listVerifications, submitVerification } from "@/api/verification";
import {
  VERIFICATION_TYPES,
  VERIFICATION_TYPE_LABELS,
  labelize,
} from "@/data/enums";
import { PageHeader } from "@/components/common/PageHeader";
import { DataTable } from "@/components/common/DataTable";
import { Pagination } from "@/components/common/Pagination";
import { StatusBadge } from "@/components/common/StatusBadge";
import { FormField } from "@/components/common/FormField";
import { Button } from "@/components/ui/button";
import { Input, Select } from "@/components/ui/input";
import { Dialog } from "@/components/ui/dialog";
import { Card, CardContent } from "@/components/ui/card";

const PAGE_SIZE = 10;

const fmtDate = (v) => (v ? new Date(v).toLocaleDateString() : "—");

export default function Verification() {
  const { user, hasRole } = useAuth();
  const companyId = user?.companyId;
  const canSubmit = hasRole("COMPANY_ADMIN", "PLATFORM_ADMIN");
  const qc = useQueryClient();
  const [page, setPage] = useState(0);
  const [submitting, setSubmitting] = useState(false);

  const query = useApiQuery(
    ["verifications", companyId, page],
    () => listVerifications(companyId, { page, size: PAGE_SIZE }),
    { enabled: Boolean(companyId) }
  );
  const data = query.data;

  const columns = [
    {
      key: "type",
      header: "Document",
      render: (v) => (
        <span className="inline-flex items-center gap-2 font-medium">
          <FileText className="size-4 text-muted-foreground" /> {labelize(v.type)}
        </span>
      ),
    },
    { key: "status", header: "Status", render: (v) => <StatusBadge status={v.status} /> },
    {
      key: "fileUrl",
      header: "File",
      render: (v) =>
        v.fileUrl ? (
          <a
            href={v.fileUrl}
            target="_blank"
            rel="noreferrer"
            className="inline-flex items-center gap-1 text-primary hover:underline"
          >
            View <ExternalLink className="size-3.5" />
          </a>
        ) : (
          "—"
        ),
    },
    { key: "reviewerNote", header: "Reviewer note", render: (v) => v.reviewerNote || "—" },
    { key: "createdAt", header: "Submitted", render: (v) => fmtDate(v.createdAt) },
  ];

  if (!companyId) {
    return (
      <div className="space-y-6">
        <PageHeader title="Verification (KYC)" />
        <Card>
          <CardContent className="p-6 text-muted-foreground">
            Your account isn't linked to a company, so there's nothing to verify.
          </CardContent>
        </Card>
      </div>
    );
  }

  return (
    <div className="space-y-6">
      <PageHeader
        title="Verification (KYC)"
        description="Submit your trade documents to get your company verified."
        actions={
          canSubmit && (
            <Button variant="accent" onClick={() => setSubmitting(true)}>
              <Plus /> Submit document
            </Button>
          )
        }
      />

      <Card className="border-gold-400/40 bg-gold-400/5">
        <CardContent className="flex items-start gap-3 p-5">
          <BadgeCheck className="mt-0.5 size-5 shrink-0 text-gold-600" />
          <p className="text-sm text-navy-800">
            Submit each required document (IEC, GST, RCMC, Bank). A platform admin reviews
            and approves them. Once approved, your company is verified and can trade.
          </p>
        </CardContent>
      </Card>

      <DataTable
        columns={columns}
        rows={data?.items}
        isLoading={query.isLoading}
        error={query.error}
        onRetry={query.refetch}
        empty={{
          icon: FileText,
          title: "No documents submitted yet",
          description: canSubmit
            ? "Submit your first KYC document to start verification."
            : "No KYC documents have been submitted for your company.",
        }}
      />
      <Pagination page={page} totalPages={data?.totalPages} onChange={setPage} />

      {submitting && (
        <SubmitDialog
          companyId={companyId}
          onClose={() => setSubmitting(false)}
          onSaved={() => {
            qc.invalidateQueries({ queryKey: ["verifications", companyId] });
            setSubmitting(false);
          }}
        />
      )}
    </div>
  );
}

const schema = z.object({
  type: z.enum(VERIFICATION_TYPES, { message: "Select a document type" }),
  fileUrl: z.string().url("Enter a valid document URL"),
});

function SubmitDialog({ companyId, onClose, onSaved }) {
  const {
    register,
    handleSubmit,
    formState: { errors },
  } = useForm({ resolver: zodResolver(schema), defaultValues: { type: "" } });

  const mutation = useApiMutation(
    (values) => submitVerification(companyId, values),
    { successMessage: "Document submitted for review", onSuccess: onSaved }
  );

  return (
    <Dialog
      open
      onClose={onClose}
      title="Submit KYC document"
      description="Provide the document type and a link to the uploaded file."
      footer={
        <>
          <Button variant="outline" onClick={onClose}>Cancel</Button>
          <Button
            variant="accent"
            onClick={handleSubmit((v) => mutation.mutate(v))}
            disabled={mutation.isPending}
          >
            {mutation.isPending ? "Submitting…" : "Submit"}
          </Button>
        </>
      }
    >
      <form className="space-y-4" onSubmit={handleSubmit((v) => mutation.mutate(v))}>
        <FormField label="Document type" error={errors.type?.message} required>
          <Select {...register("type")} defaultValue="">
            <option value="" disabled>Select…</option>
            {VERIFICATION_TYPES.map((t) => (
              <option key={t} value={t}>{VERIFICATION_TYPE_LABELS[t]}</option>
            ))}
          </Select>
        </FormField>
        <FormField
          label="Document URL"
          error={errors.fileUrl?.message}
          hint="Link to the uploaded file (file upload lands in a later phase)."
          required
        >
          <Input placeholder="https://…" {...register("fileUrl")} />
        </FormField>
      </form>
    </Dialog>
  );
}
