package com.eximplatform.sourcing.domain;

import com.eximplatform.common.domain.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * A buyer's Request For Quotation. {@code buyerCompanyId} links to a company in the identity
 * service (validated over REST); {@code hsCode} is used to match against catalog products.
 */
@Entity
@Table(name = "rfqs")
public class Rfq extends BaseEntity {

    @Column(name = "buyer_company_id", nullable = false)
    private UUID buyerCompanyId;

    @Column(nullable = false)
    private String title;

    @Column(length = 2000)
    private String description;

    @Column(name = "hs_code", nullable = false, length = 20)
    private String hsCode;

    @Column(nullable = false)
    private int quantity;

    @Column(length = 20)
    private String unit;

    @Column(name = "target_price", precision = 18, scale = 2)
    private BigDecimal targetPrice;

    @Column(length = 3)
    private String currency;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private RfqStatus status = RfqStatus.OPEN;

    public UUID getBuyerCompanyId() { return buyerCompanyId; }
    public void setBuyerCompanyId(UUID buyerCompanyId) { this.buyerCompanyId = buyerCompanyId; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getHsCode() { return hsCode; }
    public void setHsCode(String hsCode) { this.hsCode = hsCode; }
    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
    public String getUnit() { return unit; }
    public void setUnit(String unit) { this.unit = unit; }
    public BigDecimal getTargetPrice() { return targetPrice; }
    public void setTargetPrice(BigDecimal targetPrice) { this.targetPrice = targetPrice; }
    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }
    public RfqStatus getStatus() { return status; }
    public void setStatus(RfqStatus status) { this.status = status; }
}
