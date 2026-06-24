package com.eximplatform.logistics.dto;

import com.eximplatform.logistics.domain.ShipmentStatus;
import jakarta.validation.constraints.NotNull;

/** Record a tracking event, which advances the shipment to the given status. */
public class TrackingEventRequest {

    @NotNull
    private ShipmentStatus status;

    private String location;

    private String note;

    public ShipmentStatus getStatus() { return status; }
    public void setStatus(ShipmentStatus status) { this.status = status; }
    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }
    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }
}
