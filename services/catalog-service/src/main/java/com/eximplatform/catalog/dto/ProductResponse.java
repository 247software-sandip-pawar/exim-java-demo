package com.eximplatform.catalog.dto;

import com.eximplatform.catalog.domain.Product;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public class ProductResponse {

    private UUID id;
    private UUID companyId;
    private String name;
    private String description;
    private String hsCode;
    private BigDecimal unitPrice;
    private String currency;
    private String unit;
    private int minOrderQty;
    private boolean active;
    private Instant createdAt;
    private Instant updatedAt;

    public static ProductResponse from(Product p) {
        ProductResponse r = new ProductResponse();
        r.id = p.getId();
        r.companyId = p.getCompanyId();
        r.name = p.getName();
        r.description = p.getDescription();
        r.hsCode = p.getHsCode();
        r.unitPrice = p.getUnitPrice();
        r.currency = p.getCurrency();
        r.unit = p.getUnit();
        r.minOrderQty = p.getMinOrderQty();
        r.active = p.isActive();
        r.createdAt = p.getCreatedAt();
        r.updatedAt = p.getUpdatedAt();
        return r;
    }

    public UUID getId() { return id; }
    public UUID getCompanyId() { return companyId; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public String getHsCode() { return hsCode; }
    public BigDecimal getUnitPrice() { return unitPrice; }
    public String getCurrency() { return currency; }
    public String getUnit() { return unit; }
    public int getMinOrderQty() { return minOrderQty; }
    public boolean isActive() { return active; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
}
