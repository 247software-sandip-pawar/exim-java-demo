import { useQuery, useMutation } from "@tanstack/react-query";
import { toast } from "sonner";

/**
 * Thin wrappers so every module fetches data the same way.
 * queryFn / mutationFn receive the already-unwrapped ApiResponse.data
 * (see src/lib/api.js).
 */
export function useApiQuery(queryKey, queryFn, options = {}) {
  return useQuery({ queryKey, queryFn, ...options });
}

export function useApiMutation(mutationFn, options = {}) {
  const { successMessage, ...rest } = options;
  return useMutation({
    mutationFn,
    onSuccess: (...args) => {
      if (successMessage) toast.success(successMessage);
      rest.onSuccess?.(...args);
    },
    onError: (err, ...args) => {
      toast.error(err?.message || "Something went wrong");
      rest.onError?.(err, ...args);
    },
    ...rest,
  });
}
