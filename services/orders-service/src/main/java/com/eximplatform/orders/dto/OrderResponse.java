package com.eximplatform.orders.dto;

import com.eximplatform.orders.domain.Order;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public class OrderResponse {

    private UUID id;
    private UUID quoteId;
    private UUID rfqId;
    private UUID buyerCompanyId;
    private UUID sellerCompanyId;
    private String currency;
    private BigDecimal totalAmount;
    private String status;
    private List<OrderItemResponse> items;
    private Instant createdAt;
    private Instant updatedAt;

    public static OrderResponse from(Order o) {
        OrderResponse out = new OrderResponse();
        out.id = o.getId();
        out.quoteId = o.getQuoteId();
        out.rfqId = o.getRfqId();
        out.buyerCompanyId = o.getBuyerCompanyId();
        out.sellerCompanyId = o.getSellerCompanyId();
        out.currency = o.getCurrency();
        out.totalAmount = o.getTotalAmount();
        out.status = o.getStatus() != null ? o.getStatus().name() : null;
        out.items = o.getItems() != null ? o.getItems().stream().map(OrderItemResponse::from).toList() : List.of();
        out.createdAt = o.getCreatedAt();
        out.updatedAt = o.getUpdatedAt();
        return out;
    }

    public UUID getId() { return id; }
    public UUID getQuoteId() { return quoteId; }
    public UUID getRfqId() { return rfqId; }
    public UUID getBuyerCompanyId() { return buyerCompanyId; }
    public UUID getSellerCompanyId() { return sellerCompanyId; }
    public String getCurrency() { return currency; }
    public BigDecimal getTotalAmount() { return totalAmount; }
    public String getStatus() { return status; }
    public List<OrderItemResponse> getItems() { return items; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
}
