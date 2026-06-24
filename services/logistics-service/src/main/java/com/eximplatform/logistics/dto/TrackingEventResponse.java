package com.eximplatform.logistics.dto;

import com.eximplatform.logistics.domain.TrackingEvent;

import java.time.Instant;

public record TrackingEventResponse(String status, String location, String note, Instant occurredAt) {

    public static TrackingEventResponse from(TrackingEvent e) {
        return new TrackingEventResponse(
                e.getStatus() != null ? e.getStatus().name() : null,
                e.getLocation(), e.getNote(), e.getOccurredAt());
    }
}
