package com.eximplatform.payments.domain;

import java.util.Set;

/**
 * Lifecycle of a letter of credit. Drafted, issued by the bank, optionally confirmed, then settled.
 * {@link #SETTLED}, {@link #CANCELLED} and {@link #EXPIRED} are terminal.
 */
public enum LcStatus {
    DRAFT,
    ISSUED,
    CONFIRMED,
    SETTLED,
    EXPIRED,
    CANCELLED;

    /** Allowed next states from this one. */
    public Set<LcStatus> allowedNext() {
        return switch (this) {
            case DRAFT -> Set.of(ISSUED, CANCELLED);
            case ISSUED -> Set.of(CONFIRMED, SETTLED, EXPIRED, CANCELLED);
            case CONFIRMED -> Set.of(SETTLED, EXPIRED);
            case SETTLED, EXPIRED, CANCELLED -> Set.of();
        };
    }
}
