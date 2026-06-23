package com.eximplatform.quotation.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * A counter-offer to an existing quote. The rfq/buyer/seller/product are inherited from the quote
 * being countered; only the commercial terms are restated here.
 */
public class CounterQuoteRequest {

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
