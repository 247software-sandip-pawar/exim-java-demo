package com.eximplatform.logistics.dto;

import com.eximplatform.logistics.domain.TransportMode;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;
import java.util.UUID;

/** Book a shipment for an order with a chosen logistics partner. */
public class CreateShipmentRequest {

    @NotNull
    private UUID orderId;

    @NotNull
    private UUID partnerId;

    @NotNull
    private TransportMode mode;

    @NotBlank
    private String origin;

    @NotBlank
    private String destination;

    private String incoterm;

    private Instant estimatedDelivery;

    public UUID getOrderId() { return orderId; }
    public void setOrderId(UUID orderId) { this.orderId = orderId; }
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
    public Instant getEstimatedDelivery() { return estimatedDelivery; }
    public void setEstimatedDelivery(Instant estimatedDelivery) { this.estimatedDelivery = estimatedDelivery; }
}
