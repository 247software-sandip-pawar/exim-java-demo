import { Dialog } from "@/components/ui/dialog";
import { Button } from "@/components/ui/button";

/** Generic confirm/destructive dialog driven by a mutation's pending state. */
export function ConfirmDialog({
  open = true,
  title,
  description,
  body,
  confirmLabel = "Confirm",
  destructive = false,
  pending,
  onConfirm,
  onClose,
}) {
  return (
    <Dialog
      open={open}
      onClose={onClose}
      title={title}
      description={description}
      footer={
        <>
          <Button variant="outline" onClick={onClose}>Cancel</Button>
          <Button
            variant={destructive ? undefined : "accent"}
            className={destructive ? "bg-destructive text-destructive-foreground hover:bg-destructive/90" : undefined}
            onClick={onConfirm}
            disabled={pending}
          >
            {pending ? "Working…" : confirmLabel}
          </Button>
        </>
      }
    >
      {body || (
        <p className="text-sm text-muted-foreground">
          This action cannot be undone.
        </p>
      )}
    </Dialog>
  );
}
