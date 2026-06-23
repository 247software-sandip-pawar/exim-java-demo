package com.eximplatform.quotation.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/** Seller's quote submission against a buyer's RFQ. */
public class QuoteRequest {

    @NotNull
    private UUID rfqId;

    @NotNull
    private UUID sellerCompanyId;

    @NotNull
    private UUID buyerCompanyId;

    @NotNull
    private UUID productId;

    private String productName;

    @Min(value = 1, message = "quantity must be at least 1")
    private int quantity;

    private String unit;

    @NotNull
    private BigDecimal unitPrice;

    @NotBlank
    private String currency;

    private String incoterm;

    private Instant validUntil;

    private String notes;

    public UUID getRfqId() { return rfqId; }
    public void setRfqId(UUID rfqId) { this.rfqId = rfqId; }
    public UUID getSellerCompanyId() { return sellerCompanyId; }
    public void setSellerCompanyId(UUID sellerCompanyId) { this.sellerCompanyId = sellerCompanyId; }
    public UUID getBuyerCompanyId() { return buyerCompanyId; }
    public void setBuyerCompanyId(UUID buyerCompanyId) { this.buyerCompanyId = buyerCompanyId; }
    public UUID getProductId() { return productId; }
    public void setProductId(UUID productId) { this.productId = productId; }
    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }
    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
    public String getUnit() { return unit; }
    public void setUnit(String unit) { this.unit = unit; }
    public BigDecimal getUnitPrice() { return unitPrice; }
    public void setUnitPrice(BigDecimal unitPrice) { this.unitPrice = unitPrice; }
    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }
    public String getIncoterm() { return incoterm; }
    public void setIncoterm(String incoterm) { this.incoterm = incoterm; }
    public Instant getValidUntil() { return validUntil; }
    public void setValidUntil(Instant validUntil) { this.validUntil = validUntil; }
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
}
