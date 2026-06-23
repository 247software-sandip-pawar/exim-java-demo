package com.eximplatform.orders.dto;

import com.eximplatform.orders.domain.OrderItem;

import java.math.BigDecimal;
import java.util.UUID;

public record OrderItemResponse(
        UUID productId,
        String description,
        int quantity,
        String unit,
        BigDecimal unitPrice,
        BigDecimal lineTotal) {

    public static OrderItemResponse from(OrderItem item) {
        return new OrderItemResponse(item.getProductId(), item.getDescription(), item.getQuantity(),
                item.getUnit(), item.getUnitPrice(), item.getLineTotal());
    }
}
