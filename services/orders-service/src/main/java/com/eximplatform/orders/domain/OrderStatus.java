package com.eximplatform.orders.domain;

import java.util.Set;

/**
 * Lifecycle of an order. Transitions are forward-only along the fulfilment path, with cancellation
 * allowed from any non-terminal state. {@link #DELIVERED} and {@link #CANCELLED} are terminal.
 */
public enum OrderStatus {
    CREATED,
    CONFIRMED,
    IN_PRODUCTION,
    SHIPPED,
    DELIVERED,
    CANCELLED;

    /** Allowed next states from this one. */
    public Set<OrderStatus> allowedNext() {
        return switch (this) {
            case CREATED -> Set.of(CONFIRMED, CANCELLED);
            case CONFIRMED -> Set.of(IN_PRODUCTION, CANCELLED);
            case IN_PRODUCTION -> Set.of(SHIPPED, CANCELLED);
            case SHIPPED -> Set.of(DELIVERED);
            case DELIVERED, CANCELLED -> Set.of();
        };
    }
}
