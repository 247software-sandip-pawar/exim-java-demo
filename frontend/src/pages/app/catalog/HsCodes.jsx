import { useState } from "react";
import { useQuery } from "@tanstack/react-query";
import { Search, Hash } from "lucide-react";
import { useApiQuery } from "@/hooks/useApi";
import { listHsCodes, getHsCode } from "@/api/catalog";
import { PageHeader } from "@/components/common/PageHeader";
import { DataTable } from "@/components/common/DataTable";
import { Pagination } from "@/components/common/Pagination";
import { Card, CardContent } from "@/components/ui/card";
import { Badge } from "@/components/ui/badge";
import { Input } from "@/components/ui/input";
import { Button } from "@/components/ui/button";

const PAGE_SIZE = 12;

export default function HsCodes() {
  const [page, setPage] = useState(0);
  const [codeInput, setCodeInput] = useState("");
  const [lookupCode, setLookupCode] = useState("");

  const list = useApiQuery(["hs-codes", page], () =>
    listHsCodes({ page, size: PAGE_SIZE })
  );

  // Exact-code lookup (separate, only runs when a code is submitted)
  const lookup = useQuery({
    queryKey: ["hs-code", lookupCode],
    queryFn: () => getHsCode(lookupCode),
    enabled: Boolean(lookupCode),
    retry: false,
  });

  const columns = [
    {
      key: "code",
      header: "HS code",
      render: (c) => <Badge variant="outline" className="font-mono">{c.code}</Badge>,
    },
    { key: "description", header: "Description" },
  ];

  return (
    <div className="space-y-6">
      <PageHeader
        title="HS Codes"
        description="Look up Harmonized System codes used to classify traded goods."
      />

      <div className="flex gap-2">
        <div className="relative flex-1 sm:max-w-xs">
          <Search className="absolute left-3 top-1/2 size-4 -translate-y-1/2 text-muted-foreground" />
          <Input
            className="pl-9 font-mono"
            placeholder="Exact HS code, e.g. 1006.30"
            value={codeInput}
            onChange={(e) => setCodeInput(e.target.value)}
            onKeyDown={(e) => e.key === "Enter" && setLookupCode(codeInput.trim())}
          />
        </div>
        <Button variant="outline" onClick={() => setLookupCode(codeInput.trim())}>
          Look up
        </Button>
      </div>

      {lookupCode && (
        <Card className={lookup.isError ? "border-destructive/40" : "border-primary/30"}>
          <CardContent className="flex items-center gap-4 p-5">
            <span className="grid size-10 place-items-center rounded-lg bg-secondary text-primary">
              <Hash className="size-5" />
            </span>
            {lookup.isLoading ? (
              <span className="text-muted-foreground">Looking up {lookupCode}…</span>
            ) : lookup.isError ? (
              <span className="text-destructive">No HS code found for “{lookupCode}”.</span>
            ) : (
              <div>
                <div className="font-mono font-semibold text-navy-900">{lookup.data.code}</div>
                <div className="text-sm text-muted-foreground">{lookup.data.description}</div>
              </div>
            )}
          </CardContent>
        </Card>
      )}

      <DataTable
        columns={columns}
        rows={list.data?.items}
        isLoading={list.isLoading}
        error={list.error}
        onRetry={list.refetch}
        rowKey="code"
        empty={{ icon: Hash, title: "No HS codes found" }}
      />
      <Pagination page={page} totalPages={list.data?.totalPages} onChange={setPage} />
    </div>
  );
}
