import { useState } from "react";
import { Activity as ActivityIcon, Search } from "lucide-react";
import { useApiQuery } from "@/hooks/useApi";
import { listActivity } from "@/api/activity";
import { formatDateTime } from "@/lib/format";
import { PageHeader } from "@/components/common/PageHeader";
import { DataTable } from "@/components/common/DataTable";
import { Pagination } from "@/components/common/Pagination";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";

const PAGE_SIZE = 20;

const methodVariant = (m) =>
  m === "GET" ? "outline" : m === "DELETE" ? "default" : "accent";

export default function Activity() {
  const [page, setPage] = useState(0);
  const [actorInput, setActorInput] = useState("");
  const [actor, setActor] = useState("");

  const params = { page, size: PAGE_SIZE, sort: "createdAt,desc" };
  if (actor) params.actor = actor;

  const query = useApiQuery(["activity", page, actor], () => listActivity(params));
  const data = query.data;

  const applyFilter = () => {
    setActor(actorInput.trim());
    setPage(0);
  };

  const columns = [
    { key: "at", header: "When", render: (e) => formatDateTime(e.at) },
    { key: "actor", header: "Actor", render: (e) => <span className="font-medium">{e.actor}</span> },
    { key: "role", header: "Role", render: (e) => (e.role ? <Badge variant="outline">{e.role.replace(/_/g, " ")}</Badge> : "—") },
    { key: "method", header: "Method", render: (e) => <Badge variant={methodVariant(e.method)}>{e.method}</Badge> },
    { key: "path", header: "Path", render: (e) => <span className="font-mono text-xs text-navy-700">{e.path}</span> },
    { key: "status", header: "Status", render: (e) => <span className={e.status >= 400 ? "font-semibold text-destructive" : "text-navy-700"}>{e.status}</span> },
  ];

  return (
    <div className="space-y-6">
      <PageHeader
        title="Activity log"
        description="Every authenticated API action across the platform, captured at the gateway."
      />

      <div className="flex flex-wrap items-end gap-2">
        <div className="min-w-64 flex-1">
          <Input
            placeholder="Filter by actor (email)…"
            value={actorInput}
            onChange={(e) => setActorInput(e.target.value)}
            onKeyDown={(e) => e.key === "Enter" && applyFilter()}
          />
        </div>
        <Button variant="outline" onClick={applyFilter}>
          <Search /> Filter
        </Button>
        {actor && (
          <Button
            variant="ghost"
            size="sm"
            onClick={() => {
              setActorInput("");
              setActor("");
              setPage(0);
            }}
          >
            Clear
          </Button>
        )}
      </div>

      <DataTable
        columns={columns}
        rows={data?.items}
        isLoading={query.isLoading}
        error={query.error}
        onRetry={query.refetch}
        rowKey="id"
        empty={{ icon: ActivityIcon, title: "No activity recorded yet" }}
      />
      <Pagination page={page} totalPages={data?.totalPages} onChange={setPage} />
    </div>
  );
}
