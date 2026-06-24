package com.eximplatform.billing.domain;

import com.eximplatform.common.domain.BaseEntity;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;

/**
 * A subscription plan. Seeded reference data; a subscription references a plan by its code.
 * {@code maxProducts}/{@code maxRfqs} are plan limits ({@code -1} means unlimited).
 */
@Document(collection = "plans")
public class Plan extends BaseEntity {

    @Indexed(unique = true)
    private String code;

    private String name;

    private String description;

    private BigDecimal priceMonthly;

    private String currency;

    private int maxProducts;

    private int maxRfqs;

    private boolean active = true;

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public BigDecimal getPriceMonthly() { return priceMonthly; }
    public void setPriceMonthly(BigDecimal priceMonthly) { this.priceMonthly = priceMonthly; }
    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }
    public int getMaxProducts() { return maxProducts; }
    public void setMaxProducts(int maxProducts) { this.maxProducts = maxProducts; }
    public int getMaxRfqs() { return maxRfqs; }
    public void setMaxRfqs(int maxRfqs) { this.maxRfqs = maxRfqs; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
}
