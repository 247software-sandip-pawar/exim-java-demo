package com.eximplatform.messaging.domain;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * An offer attached to a message — an in-chat price proposal. Embedded inside {@link Message} (not
 * its own collection); {@code productId} links to a catalog product by bare UUID. A negotiation
 * that converges into a formal quote is handled by the quotation service, not here.
 */
public class Offer {

    private UUID productId;
    private int quantity;
    private String unit;
    private BigDecimal unitPrice;
    private String currency;
    private String incoterm;

    public UUID getProductId() { return productId; }
    public void setProductId(UUID productId) { this.productId = productId; }
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
}
