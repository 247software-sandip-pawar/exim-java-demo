package com.eximplatform.orders.controller;

import com.eximplatform.common.api.ApiResponse;
import com.eximplatform.common.api.PageResponse;
import com.eximplatform.orders.domain.OrderStatus;
import com.eximplatform.orders.dto.CreateOrderRequest;
import com.eximplatform.orders.dto.OrderResponse;
import com.eximplatform.orders.dto.OrderStatusRequest;
import com.eximplatform.orders.service.OrderService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('COMPANY_ADMIN', 'COMPANY_MEMBER', 'PLATFORM_ADMIN')")
    public ApiResponse<OrderResponse> create(@Valid @RequestBody CreateOrderRequest request) {
        return ApiResponse.ok(orderService.createFromQuote(request));
    }

    @GetMapping("/{id}")
    public ApiResponse<OrderResponse> get(@PathVariable UUID id) {
        return ApiResponse.ok(orderService.get(id));
    }

    /** List orders; optionally filter by {@code buyerCompanyId} or {@code status}. */
    @GetMapping
    public ApiResponse<PageResponse<OrderResponse>> list(
            @RequestParam(required = false) UUID buyerCompanyId,
            @RequestParam(required = false) OrderStatus status,
            Pageable pageable) {
        return ApiResponse.ok(orderService.list(buyerCompanyId, status, pageable));
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('COMPANY_ADMIN', 'COMPANY_MEMBER', 'PLATFORM_ADMIN')")
    public ApiResponse<OrderResponse> updateStatus(@PathVariable UUID id,
                                                   @Valid @RequestBody OrderStatusRequest request) {
        return ApiResponse.ok(orderService.updateStatus(id, request.getStatus()));
    }
}
