package com.eximplatform.logistics.domain;

import java.time.Instant;

/**
 * A point on a shipment's tracking history. Embedded inside {@link Shipment} (not its own
 * collection). Each event records the status reached, where, and when.
 */
public class TrackingEvent {

    private ShipmentStatus status;
    private String location;
    private String note;
    private Instant occurredAt;

    public TrackingEvent() {
    }

    public TrackingEvent(ShipmentStatus status, String location, String note, Instant occurredAt) {
        this.status = status;
        this.location = location;
        this.note = note;
        this.occurredAt = occurredAt;
    }

    public ShipmentStatus getStatus() { return status; }
    public void setStatus(ShipmentStatus status) { this.status = status; }
    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }
    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }
    public Instant getOccurredAt() { return occurredAt; }
    public void setOccurredAt(Instant occurredAt) { this.occurredAt = occurredAt; }
}
