package com.eximplatform.documents.dto;

import com.eximplatform.documents.domain.TradeDocument;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public class DocumentResponse {

    private UUID id;
    private UUID orderId;
    private UUID buyerCompanyId;
    private UUID sellerCompanyId;
    private String type;
    private String documentNumber;
    private BigDecimal totalAmount;
    private String currency;
    private String fileUrl;
    private String status;
    private Instant createdAt;
    private Instant updatedAt;

    public static DocumentResponse from(TradeDocument d) {
        DocumentResponse out = new DocumentResponse();
        out.id = d.getId();
        out.orderId = d.getOrderId();
        out.buyerCompanyId = d.getBuyerCompanyId();
        out.sellerCompanyId = d.getSellerCompanyId();
        out.type = d.getType() != null ? d.getType().name() : null;
        out.documentNumber = d.getDocumentNumber();
        out.totalAmount = d.getTotalAmount();
        out.currency = d.getCurrency();
        out.fileUrl = d.getFileUrl();
        out.status = d.getStatus() != null ? d.getStatus().name() : null;
        out.createdAt = d.getCreatedAt();
        out.updatedAt = d.getUpdatedAt();
        return out;
    }

    public UUID getId() { return id; }
    public UUID getOrderId() { return orderId; }
    public UUID getBuyerCompanyId() { return buyerCompanyId; }
    public UUID getSellerCompanyId() { return sellerCompanyId; }
    public String getType() { return type; }
    public String getDocumentNumber() { return documentNumber; }
    public BigDecimal getTotalAmount() { return totalAmount; }
    public String getCurrency() { return currency; }
    public String getFileUrl() { return fileUrl; }
    public String getStatus() { return status; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
}
