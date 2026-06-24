package com.eximplatform.trust.domain;

import java.util.Set;

/**
 * Lifecycle of a dispute. {@link #RESOLVED}, {@link #REJECTED} and {@link #CANCELLED} are terminal.
 */
public enum DisputeStatus {
    OPEN,
    UNDER_REVIEW,
    RESOLVED,
    REJECTED,
    CANCELLED;

    /** Allowed next states from this one. */
    public Set<DisputeStatus> allowedNext() {
        return switch (this) {
            case OPEN -> Set.of(UNDER_REVIEW, CANCELLED);
            case UNDER_REVIEW -> Set.of(RESOLVED, REJECTED);
            case RESOLVED, REJECTED, CANCELLED -> Set.of();
        };
    }
}
