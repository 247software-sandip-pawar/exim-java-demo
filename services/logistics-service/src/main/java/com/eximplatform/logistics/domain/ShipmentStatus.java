package com.eximplatform.logistics.domain;

import java.util.Set;

/**
 * Lifecycle of a shipment. Forward-only along the fulfilment path, with cancellation allowed from
 * any non-terminal state. {@link #DELIVERED} and {@link #CANCELLED} are terminal.
 */
public enum ShipmentStatus {
    CREATED,
    BOOKED,
    IN_TRANSIT,
    ARRIVED,
    DELIVERED,
    CANCELLED;

    /** Allowed next states from this one. */
    public Set<ShipmentStatus> allowedNext() {
        return switch (this) {
            case CREATED -> Set.of(BOOKED, CANCELLED);
            case BOOKED -> Set.of(IN_TRANSIT, CANCELLED);
            case IN_TRANSIT -> Set.of(ARRIVED, CANCELLED);
            case ARRIVED -> Set.of(DELIVERED);
            case DELIVERED, CANCELLED -> Set.of();
        };
    }
}
