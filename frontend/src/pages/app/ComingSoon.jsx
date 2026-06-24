import { Hammer } from "lucide-react";
import { PageHeader } from "@/components/common/PageHeader";
import { EmptyState } from "@/components/common/EmptyState";

/** Placeholder for modules that ship in later phases. */
export default function ComingSoon({ title, phase }) {
  return (
    <div className="space-y-8">
      <PageHeader title={title} description="This module is part of an upcoming phase." />
      <EmptyState
        icon={Hammer}
        title={`${title} is coming soon`}
        description={
          phase
            ? `Planned for ${phase}. The foundation (auth, API client, shell) is ready to wire it up.`
            : "The foundation is ready — this screen will be wired to its backend service next."
        }
      />
    </div>
  );
}
