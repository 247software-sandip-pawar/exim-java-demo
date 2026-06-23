package com.eximplatform.orders.dto;

import com.eximplatform.orders.domain.OrderStatus;
import jakarta.validation.constraints.NotNull;

/** Advance an order to a new status (validated against the allowed transitions). */
public class OrderStatusRequest {

    @NotNull
    private OrderStatus status;

    public OrderStatus getStatus() { return status; }
    public void setStatus(OrderStatus status) { this.status = status; }
}
