package com.eximplatform.orders.domain;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * A line on an order. Embedded inside {@link Order} (not its own collection); {@code productId}
 * links to a catalog product by bare UUID.
 */
public class OrderItem {

    private UUID productId;
    private String description;
    private int quantity;
    private String unit;
    private BigDecimal unitPrice;
    private BigDecimal lineTotal;

    public OrderItem() {
    }

    public OrderItem(UUID productId, String description, int quantity, String unit,
                     BigDecimal unitPrice, BigDecimal lineTotal) {
        this.productId = productId;
        this.description = description;
        this.quantity = quantity;
        this.unit = unit;
        this.unitPrice = unitPrice;
        this.lineTotal = lineTotal;
    }

    public UUID getProductId() { return productId; }
    public void setProductId(UUID productId) { this.productId = productId; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
    public String getUnit() { return unit; }
    public void setUnit(String unit) { this.unit = unit; }
    public BigDecimal getUnitPrice() { return unitPrice; }
    public void setUnitPrice(BigDecimal unitPrice) { this.unitPrice = unitPrice; }
    public BigDecimal getLineTotal() { return lineTotal; }
    public void setLineTotal(BigDecimal lineTotal) { this.lineTotal = lineTotal; }
}
