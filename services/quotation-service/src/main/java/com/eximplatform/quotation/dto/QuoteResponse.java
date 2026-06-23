package com.eximplatform.quotation.dto;

import com.eximplatform.quotation.domain.Quote;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public class QuoteResponse {

    private UUID id;
    private UUID rfqId;
    private UUID sellerCompanyId;
    private UUID buyerCompanyId;
    private UUID productId;
    private String productName;
    private int quantity;
    private String unit;
    private BigDecimal unitPrice;
    private String currency;
    private String incoterm;
    private Instant validUntil;
    private String notes;
    private String status;
    private UUID parentQuoteId;
    private Instant createdAt;
    private Instant updatedAt;

    public static QuoteResponse from(Quote q) {
        QuoteResponse out = new QuoteResponse();
        out.id = q.getId();
        out.rfqId = q.getRfqId();
        out.sellerCompanyId = q.getSellerCompanyId();
        out.buyerCompanyId = q.getBuyerCompanyId();
        out.productId = q.getProductId();
        out.productName = q.getProductName();
        out.quantity = q.getQuantity();
        out.unit = q.getUnit();
        out.unitPrice = q.getUnitPrice();
        out.currency = q.getCurrency();
        out.incoterm = q.getIncoterm();
        out.validUntil = q.getValidUntil();
        out.notes = q.getNotes();
        out.status = q.getStatus() != null ? q.getStatus().name() : null;
        out.parentQuoteId = q.getParentQuoteId();
        out.createdAt = q.getCreatedAt();
        out.updatedAt = q.getUpdatedAt();
        return out;
    }

    public UUID getId() { return id; }
    public UUID getRfqId() { return rfqId; }
    public UUID getSellerCompanyId() { return sellerCompanyId; }
    public UUID getBuyerCompanyId() { return buyerCompanyId; }
    public UUID getProductId() { return productId; }
    public String getProductName() { return productName; }
    public int getQuantity() { return quantity; }
    public String getUnit() { return unit; }
    public BigDecimal getUnitPrice() { return unitPrice; }
    public String getCurrency() { return currency; }
    public String getIncoterm() { return incoterm; }
    public Instant getValidUntil() { return validUntil; }
    public String getNotes() { return notes; }
    public String getStatus() { return status; }
    public UUID getParentQuoteId() { return parentQuoteId; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
}
