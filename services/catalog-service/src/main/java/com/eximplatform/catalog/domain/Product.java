package com.eximplatform.catalog.domain;

import com.eximplatform.common.domain.BaseEntity;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * A product listed by a seller company. {@code companyId} links to a company in the identity
 * service (validated over REST on create); {@code hsCode} links to an {@link HsCode} by its code.
 */
@Document(collection = "products")
public class Product extends BaseEntity {

    @Indexed
    private UUID companyId;

    private String name;

    private String description;

    @Indexed
    private String hsCode;

    private BigDecimal unitPrice;

    private String currency;

    private String unit;

    private int minOrderQty = 1;

    private boolean active = true;

    public UUID getCompanyId() { return companyId; }
    public void setCompanyId(UUID companyId) { this.companyId = companyId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getHsCode() { return hsCode; }
    public void setHsCode(String hsCode) { this.hsCode = hsCode; }
    public BigDecimal getUnitPrice() { return unitPrice; }
    public void setUnitPrice(BigDecimal unitPrice) { this.unitPrice = unitPrice; }
    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }
    public String getUnit() { return unit; }
    public void setUnit(String unit) { this.unit = unit; }
    public int getMinOrderQty() { return minOrderQty; }
    public void setMinOrderQty(int minOrderQty) { this.minOrderQty = minOrderQty; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
}
