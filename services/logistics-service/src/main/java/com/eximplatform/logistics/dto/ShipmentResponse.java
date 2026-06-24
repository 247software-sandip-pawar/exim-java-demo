package com.eximplatform.logistics.dto;

import com.eximplatform.logistics.domain.Shipment;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public class ShipmentResponse {

    private UUID id;
    private UUID orderId;
    private UUID buyerCompanyId;
    private UUID sellerCompanyId;
    private UUID partnerId;
    private String mode;
    private String origin;
    private String destination;
    private String incoterm;
    private String trackingNumber;
    private Instant estimatedDelivery;
    private String status;
    private List<TrackingEventResponse> events;
    private Instant createdAt;
    private Instant updatedAt;

    public static ShipmentResponse from(Shipment s) {
        ShipmentResponse out = new ShipmentResponse();
        out.id = s.getId();
        out.orderId = s.getOrderId();
        out.buyerCompanyId = s.getBuyerCompanyId();
        out.sellerCompanyId = s.getSellerCompanyId();
        out.partnerId = s.getPartnerId();
        out.mode = s.getMode() != null ? s.getMode().name() : null;
        out.origin = s.getOrigin();
        out.destination = s.getDestination();
        out.incoterm = s.getIncoterm();
        out.trackingNumber = s.getTrackingNumber();
        out.estimatedDelivery = s.getEstimatedDelivery();
        out.status = s.getStatus() != null ? s.getStatus().name() : null;
        out.events = s.getEvents() != null ? s.getEvents().stream().map(TrackingEventResponse::from).toList() : List.of();
        out.createdAt = s.getCreatedAt();
        out.updatedAt = s.getUpdatedAt();
        return out;
    }

    public UUID getId() { return id; }
    public UUID getOrderId() { return orderId; }
    public UUID getBuyerCompanyId() { return buyerCompanyId; }
    public UUID getSellerCompanyId() { return sellerCompanyId; }
    public UUID getPartnerId() { return partnerId; }
    public String getMode() { return mode; }
    public String getOrigin() { return origin; }
    public String getDestination() { return destination; }
    public String getIncoterm() { return incoterm; }
    public String getTrackingNumber() { return trackingNumber; }
    public Instant getEstimatedDelivery() { return estimatedDelivery; }
    public String getStatus() { return status; }
    public List<TrackingEventResponse> getEvents() { return events; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
}
