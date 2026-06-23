package com.eximplatform.orders.domain;

import com.eximplatform.common.domain.BaseEntity;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * An order created from an accepted quote. {@code quoteId} is unique so a given accepted quote
 * yields at most one order (the idempotency key for order creation); buyer/seller company ids and
 * {@code rfqId} link to other services by bare UUID. Optimistic locking comes from
 * {@link BaseEntity}'s {@code @Version}.
 */
@Document(collection = "orders")
public class Order extends BaseEntity {

    @Indexed(unique = true)
    private UUID quoteId;

    private UUID rfqId;

    @Indexed
    private UUID buyerCompanyId;

    @Indexed
    private UUID sellerCompanyId;

    private String currency;

    private BigDecimal totalAmount;

    private OrderStatus status = OrderStatus.CREATED;

    private List<OrderItem> items = new ArrayList<>();

    public UUID getQuoteId() { return quoteId; }
    public void setQuoteId(UUID quoteId) { this.quoteId = quoteId; }
    public UUID getRfqId() { return rfqId; }
    public void setRfqId(UUID rfqId) { this.rfqId = rfqId; }
    public UUID getBuyerCompanyId() { return buyerCompanyId; }
    public void setBuyerCompanyId(UUID buyerCompanyId) { this.buyerCompanyId = buyerCompanyId; }
    public UUID getSellerCompanyId() { return sellerCompanyId; }
    public void setSellerCompanyId(UUID sellerCompanyId) { this.sellerCompanyId = sellerCompanyId; }
    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }
    public BigDecimal getTotalAmount() { return totalAmount; }
    public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }
    public OrderStatus getStatus() { return status; }
    public void setStatus(OrderStatus status) { this.status = status; }
    public List<OrderItem> getItems() { return items; }
    public void setItems(List<OrderItem> items) { this.items = items; }
}
