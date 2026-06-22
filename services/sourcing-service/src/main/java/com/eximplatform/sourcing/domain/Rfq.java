package com.eximplatform.sourcing.domain;

import com.eximplatform.common.domain.BaseEntity;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * A buyer's Request For Quotation. {@code buyerCompanyId} links to a company in the identity
 * service (validated over REST); {@code hsCode} is used to match against catalog products.
 */
@Document(collection = "rfqs")
public class Rfq extends BaseEntity {

    @Indexed
    private UUID buyerCompanyId;

    private String title;

    private String description;

    @Indexed
    private String hsCode;

    private int quantity;

    private String unit;

    private BigDecimal targetPrice;

    private String currency;

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
