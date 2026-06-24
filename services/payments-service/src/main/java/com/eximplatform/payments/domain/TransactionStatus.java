package com.eximplatform.payments.domain;

import java.util.Set;

/**
 * Lifecycle of an escrow transaction. Money is initiated, funded into escrow, then either released
 * to the seller or refunded to the buyer. {@link #RELEASED}, {@link #REFUNDED} and {@link #FAILED}
 * are terminal.
 */
public enum TransactionStatus {
    INITIATED,
    FUNDED,
    RELEASED,
    REFUNDED,
    FAILED;

    /** Allowed next states from this one. */
    public Set<TransactionStatus> allowedNext() {
        return switch (this) {
            case INITIATED -> Set.of(FUNDED, FAILED);
            case FUNDED -> Set.of(RELEASED, REFUNDED);
            case RELEASED, REFUNDED, FAILED -> Set.of();
        };
    }
}
