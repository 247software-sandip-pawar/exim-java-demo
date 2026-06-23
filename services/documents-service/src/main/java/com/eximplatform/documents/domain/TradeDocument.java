package com.eximplatform.documents.domain;

import com.eximplatform.common.domain.BaseEntity;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * A trade document generated for an order. {@code orderId} and the buyer/seller company ids link to
 * other services by bare UUID. {@code fileUrl} is a placeholder for the rendered file — real PDF
 * generation and object storage are deferred (Phase 4), as with verification's KYC fileUrl.
 */
@Document(collection = "trade_documents")
public class TradeDocument extends BaseEntity {

    @Indexed
    private UUID orderId;

    private UUID buyerCompanyId;

    private UUID sellerCompanyId;

    private DocumentType type;

    @Indexed(unique = true)
    private String documentNumber;

    private BigDecimal totalAmount;

    private String currency;

    /** Location of the rendered document. Placeholder until a StorageService/PDF renderer exists. */
    private String fileUrl;

    private DocumentStatus status = DocumentStatus.GENERATED;

    public UUID getOrderId() { return orderId; }
    public void setOrderId(UUID orderId) { this.orderId = orderId; }
    public UUID getBuyerCompanyId() { return buyerCompanyId; }
    public void setBuyerCompanyId(UUID buyerCompanyId) { this.buyerCompanyId = buyerCompanyId; }
    public UUID getSellerCompanyId() { return sellerCompanyId; }
    public void setSellerCompanyId(UUID sellerCompanyId) { this.sellerCompanyId = sellerCompanyId; }
    public DocumentType getType() { return type; }
    public void setType(DocumentType type) { this.type = type; }
    public String getDocumentNumber() { return documentNumber; }
    public void setDocumentNumber(String documentNumber) { this.documentNumber = documentNumber; }
    public BigDecimal getTotalAmount() { return totalAmount; }
    public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }
    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }
    public String getFileUrl() { return fileUrl; }
    public void setFileUrl(String fileUrl) { this.fileUrl = fileUrl; }
    public DocumentStatus getStatus() { return status; }
    public void setStatus(DocumentStatus status) { this.status = status; }
}
