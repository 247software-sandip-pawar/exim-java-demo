package com.eximplatform.quotation.domain;

import com.eximplatform.common.domain.BaseEntity;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * A seller's price offer for a buyer's RFQ. {@code rfqId} links to a sourcing RFQ; the buyer/seller
 * company ids link to companies in the identity service; {@code productId} links to a catalog
 * product. All links are bare UUIDs validated over REST, never by touching another service's DB.
 *
 * <p>A counter-offer is modelled as a new quote whose {@code parentQuoteId} points at the quote it
 * supersedes (which is moved to {@link QuoteStatus#COUNTERED}).
 */
@Document(collection = "quotes")
public class Quote extends BaseEntity {

    @Indexed
    private UUID rfqId;

    @Indexed
    private UUID sellerCompanyId;

    @Indexed
    private UUID buyerCompanyId;

    private UUID productId;

    private String productName;

    private int quantity;

    private String unit;

    private BigDecimal unitPrice;

    private String currency;

    /** Incoterm for the offer, e.g. FOB, CIF, EXW. */
    private String incoterm;

    private Instant validUntil;

    private String notes;

    private QuoteStatus status = QuoteStatus.SUBMITTED;

    /** When this quote is a counter-offer, the id of the quote it supersedes; null otherwise. */
    private UUID parentQuoteId;

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
    public QuoteStatus getStatus() { return status; }
    public void setStatus(QuoteStatus status) { this.status = status; }
    public UUID getParentQuoteId() { return parentQuoteId; }
    public void setParentQuoteId(UUID parentQuoteId) { this.parentQuoteId = parentQuoteId; }
}
