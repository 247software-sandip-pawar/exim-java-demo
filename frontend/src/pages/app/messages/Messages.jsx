import { useState } from "react";
import { useForm } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import { z } from "zod";
import { useQueryClient } from "@tanstack/react-query";
import { MessagesSquare, Plus, Send } from "lucide-react";
import { useAuth } from "@/auth/AuthContext";
import { useApiQuery, useApiMutation } from "@/hooks/useApi";
import {
  listConversations,
  listMessages,
  postMessage,
  createConversation,
} from "@/api/messaging";
import { listCompanies } from "@/api/identity";
import { formatMoney, formatDateTime } from "@/lib/format";
import { PageHeader } from "@/components/common/PageHeader";
import { LoadingState } from "@/components/common/LoadingState";
import { ErrorState } from "@/components/common/ErrorState";
import { EmptyState } from "@/components/common/EmptyState";
import { FormField } from "@/components/common/FormField";
import { Card } from "@/components/ui/card";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { Input, Select } from "@/components/ui/input";
import { Dialog } from "@/components/ui/dialog";
import { cn } from "@/lib/utils";

export default function Messages() {
  const { user } = useAuth();
  const [selectedId, setSelectedId] = useState(null);
  const [creating, setCreating] = useState(false);

  const convosQuery = useApiQuery(
    ["conversations", user?.companyId],
    () => listConversations({ participantCompanyId: user?.companyId, size: 50 }),
    { enabled: Boolean(user?.companyId) }
  );
  const conversations = convosQuery.data?.items || [];

  return (
    <div className="space-y-6">
      <PageHeader
        title="Messages"
        description="Talk to buyers and sellers. Updates poll every few seconds (no WebSocket yet)."
        actions={
          <Button variant="accent" onClick={() => setCreating(true)}>
            <Plus /> New conversation
          </Button>
        }
      />

      <div className="grid gap-4 lg:grid-cols-[20rem_1fr]">
        {/* Conversation list */}
        <Card className="overflow-hidden">
          {convosQuery.isLoading ? (
            <LoadingState />
          ) : convosQuery.error ? (
            <ErrorState error={convosQuery.error} onRetry={convosQuery.refetch} />
          ) : conversations.length === 0 ? (
            <EmptyState
              icon={MessagesSquare}
              title="No conversations"
              description="Start one to reach a counterparty."
            />
          ) : (
            <ul className="divide-y divide-border">
              {conversations.map((c) => (
                <li key={c.id}>
                  <button
                    onClick={() => setSelectedId(c.id)}
                    className={cn(
                      "w-full px-4 py-3 text-left transition-colors hover:bg-secondary/40",
                      selectedId === c.id && "bg-secondary/60"
                    )}
                  >
                    <div className="flex items-center justify-between gap-2">
                      <span className="truncate font-medium text-navy-900">{c.subject}</span>
                      <Badge variant={c.status === "OPEN" ? "teal" : "outline"}>{c.status}</Badge>
                    </div>
                    <p className="mt-0.5 text-xs text-muted-foreground">
                      {c.participantCompanyIds?.length || 0} participants
                    </p>
                  </button>
                </li>
              ))}
            </ul>
          )}
        </Card>

        {/* Thread */}
        {selectedId ? (
          <Thread conversationId={selectedId} />
        ) : (
          <Card className="grid place-items-center p-10">
            <EmptyState
              icon={MessagesSquare}
              title="Select a conversation"
              description="Pick a thread on the left to read and reply."
            />
          </Card>
        )}
      </div>

      {creating && (
        <NewConversationDialog
          onClose={() => setCreating(false)}
          onSaved={(c) => {
            convosQuery.refetch();
            setCreating(false);
            if (c?.id) setSelectedId(c.id);
          }}
        />
      )}
    </div>
  );
}

function Thread({ conversationId }) {
  const { user } = useAuth();
  const qc = useQueryClient();
  const [body, setBody] = useState("");

  // Poll for new messages — no WebSocket on the backend yet.
  const query = useApiQuery(
    ["messages", conversationId],
    () => listMessages(conversationId, { size: 100 }),
    { refetchInterval: 5000 }
  );

  const messages = [...(query.data?.items || [])].sort(
    (a, b) => new Date(a.createdAt) - new Date(b.createdAt)
  );

  const mutation = useApiMutation(
    (text) =>
      postMessage(conversationId, {
        senderCompanyId: user?.companyId,
        senderUserId: user?.id,
        body: text,
      }),
    {
      onSuccess: () => {
        setBody("");
        qc.invalidateQueries({ queryKey: ["messages", conversationId] });
      },
    }
  );

  const send = () => {
    const text = body.trim();
    if (text) mutation.mutate(text);
  };

  return (
    <Card className="flex h-[60vh] flex-col">
      <div className="flex-1 space-y-3 overflow-y-auto p-4">
        {query.isLoading ? (
          <LoadingState />
        ) : query.error ? (
          <ErrorState error={query.error} onRetry={query.refetch} />
        ) : messages.length === 0 ? (
          <EmptyState icon={MessagesSquare} title="No messages yet" description="Say hello." />
        ) : (
          messages.map((m) => {
            const mine = m.senderCompanyId === user?.companyId;
            return (
              <div key={m.id} className={cn("flex", mine ? "justify-end" : "justify-start")}>
                <div
                  className={cn(
                    "max-w-[75%] rounded-2xl px-4 py-2.5 text-sm",
                    mine ? "bg-primary text-primary-foreground" : "bg-secondary text-navy-900"
                  )}
                >
                  <p className="whitespace-pre-wrap">{m.body}</p>
                  {m.offer && (
                    <div
                      className={cn(
                        "mt-2 rounded-lg border px-3 py-2 text-xs",
                        mine ? "border-white/30" : "border-border"
                      )}
                    >
                      <p className="font-semibold">Offer</p>
                      <p>
                        {m.offer.quantity} {m.offer.unit} @{" "}
                        {formatMoney(m.offer.unitPrice, m.offer.currency)}
                        {m.offer.incoterm ? ` · ${m.offer.incoterm}` : ""}
                      </p>
                    </div>
                  )}
                  <p className={cn("mt-1 text-[10px]", mine ? "text-white/70" : "text-muted-foreground")}>
                    {formatDateTime(m.createdAt)}
                  </p>
                </div>
              </div>
            );
          })
        )}
      </div>
      <div className="flex items-center gap-2 border-t border-border p-3">
        <Input
          placeholder="Type a message…"
          value={body}
          onChange={(e) => setBody(e.target.value)}
          onKeyDown={(e) => {
            if (e.key === "Enter" && !e.shiftKey) {
              e.preventDefault();
              send();
            }
          }}
        />
        <Button variant="accent" onClick={send} disabled={mutation.isPending || !body.trim()}>
          <Send /> Send
        </Button>
      </div>
    </Card>
  );
}

const schema = z.object({
  subject: z.string().min(2, "Subject is required"),
  counterpartyId: z.string().uuid("Pick a company"),
});

function NewConversationDialog({ onClose, onSaved }) {
  const { user } = useAuth();
  const companiesQuery = useApiQuery(["companies", "picker"], () => listCompanies({ size: 100 }));
  const companies = (companiesQuery.data?.items || []).filter((c) => c.id !== user?.companyId);

  const {
    register,
    handleSubmit,
    formState: { errors },
  } = useForm({ resolver: zodResolver(schema) });

  const mutation = useApiMutation(
    (values) =>
      createConversation({
        subject: values.subject,
        participantCompanyIds: [user?.companyId, values.counterpartyId],
      }),
    { successMessage: "Conversation started", onSuccess: onSaved }
  );

  return (
    <Dialog
      open
      onClose={onClose}
      title="New conversation"
      description="Start a thread with another company on the platform."
      footer={
        <>
          <Button variant="outline" onClick={onClose}>Cancel</Button>
          <Button
            variant="accent"
            onClick={handleSubmit((v) => mutation.mutate(v))}
            disabled={mutation.isPending}
          >
            {mutation.isPending ? "Starting…" : "Start"}
          </Button>
        </>
      }
    >
      <form className="space-y-4" onSubmit={handleSubmit((v) => mutation.mutate(v))}>
        <FormField label="Subject" error={errors.subject?.message} required>
          <Input placeholder="e.g. Basmati rice — pricing" {...register("subject")} />
        </FormField>
        <FormField
          label="Counterparty"
          error={errors.counterpartyId?.message}
          required
          hint={companiesQuery.isLoading ? "Loading companies…" : undefined}
        >
          <Select {...register("counterpartyId")}>
            <option value="">Select a company…</option>
            {companies.map((c) => (
              <option key={c.id} value={c.id}>{c.name}</option>
            ))}
          </Select>
        </FormField>
      </form>
    </Dialog>
  );
}
