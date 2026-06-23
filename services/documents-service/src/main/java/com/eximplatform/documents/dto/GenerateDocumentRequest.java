package com.eximplatform.documents.dto;

import com.eximplatform.documents.domain.DocumentType;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

/** Generate a trade document of the given type for an order (read from the orders service). */
public class GenerateDocumentRequest {

    @NotNull
    private UUID orderId;

    @NotNull
    private DocumentType type;

    public UUID getOrderId() { return orderId; }
    public void setOrderId(UUID orderId) { this.orderId = orderId; }
    public DocumentType getType() { return type; }
    public void setType(DocumentType type) { this.type = type; }
}
