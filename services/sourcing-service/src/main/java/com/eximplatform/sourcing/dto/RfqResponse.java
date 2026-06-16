package com.eximplatform.sourcing.dto;

import com.eximplatform.sourcing.domain.Rfq;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public class RfqResponse {

    private UUID id;
    private UUID buyerCompanyId;
    private String title;
    private String description;
    private String hsCode;
    private int quantity;
    private String unit;
    private BigDecimal targetPrice;
    private String currency;
    private String status;
    private Instant createdAt;
    private Instant updatedAt;

    public static RfqResponse from(Rfq r) {
        RfqResponse out = new RfqResponse();
        out.id = r.getId();
        out.buyerCompanyId = r.getBuyerCompanyId();
        out.title = r.getTitle();
        out.description = r.getDescription();
        out.hsCode = r.getHsCode();
        out.quantity = r.getQuantity();
        out.unit = r.getUnit();
        out.targetPrice = r.getTargetPrice();
        out.currency = r.getCurrency();
        out.status = r.getStatus() != null ? r.getStatus().name() : null;
        out.createdAt = r.getCreatedAt();
        out.updatedAt = r.getUpdatedAt();
        return out;
    }

    public UUID getId() { return id; }
    public UUID getBuyerCompanyId() { return buyerCompanyId; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public String getHsCode() { return hsCode; }
    public int getQuantity() { return quantity; }
    public String getUnit() { return unit; }
    public BigDecimal getTargetPrice() { return targetPrice; }
    public String getCurrency() { return currency; }
    public String getStatus() { return status; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
}
