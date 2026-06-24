package com.eximplatform.logistics.domain;

import com.eximplatform.common.domain.BaseEntity;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * A shipment fulfilling an order. {@code orderId} and the buyer/seller company ids link to other
 * services by bare UUID; {@code partnerId} links to a {@link LogisticsPartner}. The tracking history
 * is an embedded list of {@link TrackingEvent}s; {@code status} mirrors the latest event.
 */
@Document(collection = "shipments")
public class Shipment extends BaseEntity {

    @Indexed
    private UUID orderId;

    private UUID buyerCompanyId;

    private UUID sellerCompanyId;

    @Indexed
    private UUID partnerId;

    private TransportMode mode;

    private String origin;

    private String destination;

    private String incoterm;

    @Indexed(unique = true)
    private String trackingNumber;

    private Instant estimatedDelivery;

    private ShipmentStatus status = ShipmentStatus.CREATED;

    private List<TrackingEvent> events = new ArrayList<>();

    public UUID getOrderId() { return orderId; }
    public void setOrderId(UUID orderId) { this.orderId = orderId; }
    public UUID getBuyerCompanyId() { return buyerCompanyId; }
    public void setBuyerCompanyId(UUID buyerCompanyId) { this.buyerCompanyId = buyerCompanyId; }
    public UUID getSellerCompanyId() { return sellerCompanyId; }
    public void setSellerCompanyId(UUID sellerCompanyId) { this.sellerCompanyId = sellerCompanyId; }
    public UUID getPartnerId() { return partnerId; }
    public void setPartnerId(UUID partnerId) { this.partnerId = partnerId; }
    public TransportMode getMode() { return mode; }
    public void setMode(TransportMode mode) { this.mode = mode; }
    public String getOrigin() { return origin; }
    public void setOrigin(String origin) { this.origin = origin; }
    public String getDestination() { return destination; }
    public void setDestination(String destination) { this.destination = destination; }
    public String getIncoterm() { return incoterm; }
    public void setIncoterm(String incoterm) { this.incoterm = incoterm; }
    public String getTrackingNumber() { return trackingNumber; }
    public void setTrackingNumber(String trackingNumber) { this.trackingNumber = trackingNumber; }
    public Instant getEstimatedDelivery() { return estimatedDelivery; }
    public void setEstimatedDelivery(Instant estimatedDelivery) { this.estimatedDelivery = estimatedDelivery; }
    public ShipmentStatus getStatus() { return status; }
    public void setStatus(ShipmentStatus status) { this.status = status; }
    public List<TrackingEvent> getEvents() { return events; }
    public void setEvents(List<TrackingEvent> events) { this.events = events; }
}
