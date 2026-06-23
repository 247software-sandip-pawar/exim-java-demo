package com.eximplatform.orders.service;

import com.eximplatform.common.api.PageResponse;
import com.eximplatform.common.client.OrderView;
import com.eximplatform.common.client.QuotationClient;
import com.eximplatform.common.client.QuoteView;
import com.eximplatform.common.exception.BusinessException;
import com.eximplatform.common.exception.NotFoundException;
import com.eximplatform.orders.domain.Order;
import com.eximplatform.orders.domain.OrderItem;
import com.eximplatform.orders.domain.OrderStatus;
import com.eximplatform.orders.dto.CreateOrderRequest;
import com.eximplatform.orders.dto.OrderResponse;
import com.eximplatform.orders.repository.OrderRepository;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

/**
 * Turns an accepted quote into an order. The quote is read from the quotation service over REST
 * (never by touching its database). Order creation is idempotent per quote: the unique
 * {@code quoteId} means a repeated create for the same accepted quote returns the existing order
 * instead of creating a duplicate. MongoDB has no dirty-checking, so updates call {@code save}.
 */
@Service
@Transactional(transactionManager = "ordersTransactionManager")
public class OrderService {

    private final OrderRepository orderRepository;
    private final QuotationClient quotationClient;

    public OrderService(OrderRepository orderRepository, QuotationClient quotationClient) {
        this.orderRepository = orderRepository;
        this.quotationClient = quotationClient;
    }

    public OrderResponse createFromQuote(CreateOrderRequest req) {
        UUID quoteId = req.getQuoteId();

        // Idempotency: one order per accepted quote. Return the existing order on a repeat.
        var existing = orderRepository.findByQuoteId(quoteId);
        if (existing.isPresent()) {
            return OrderResponse.from(existing.get());
        }

        QuoteView quote = quotationClient.getQuote(quoteId)
                .orElseThrow(() -> new NotFoundException("Quote not found."));
        if (!"ACCEPTED".equals(quote.status())) {
            throw new BusinessException("QUOTE_NOT_ACCEPTED",
                    "An order can only be created from an ACCEPTED quote; quote is " + quote.status() + ".");
        }

        BigDecimal lineTotal = quote.unitPrice().multiply(BigDecimal.valueOf(quote.quantity()));
        OrderItem item = new OrderItem(quote.productId(), quote.productName(), quote.quantity(),
                quote.unit(), quote.unitPrice(), lineTotal);

        Order order = new Order();
        order.setQuoteId(quoteId);
        order.setRfqId(quote.rfqId());
        order.setBuyerCompanyId(quote.buyerCompanyId());
        order.setSellerCompanyId(quote.sellerCompanyId());
        order.setCurrency(quote.currency());
        order.setItems(List.of(item));
        order.setTotalAmount(lineTotal);
        order.setStatus(OrderStatus.CREATED);
        return OrderResponse.from(orderRepository.save(order));
    }

    @Transactional(transactionManager = "ordersTransactionManager", readOnly = true)
    public OrderResponse get(UUID id) {
        return OrderResponse.from(findOrThrow(id));
    }

    @Transactional(transactionManager = "ordersTransactionManager", readOnly = true)
    public PageResponse<OrderResponse> list(UUID buyerCompanyId, OrderStatus status, Pageable pageable) {
        var page = buyerCompanyId != null
                ? orderRepository.findByBuyerCompanyId(buyerCompanyId, pageable)
                : status != null
                ? orderRepository.findByStatus(status, pageable)
                : orderRepository.findAll(pageable);
        return PageResponse.from(page, OrderResponse::from);
    }

    public OrderResponse updateStatus(UUID id, OrderStatus target) {
        Order order = findOrThrow(id);
        if (!order.getStatus().allowedNext().contains(target)) {
            throw new BusinessException("INVALID_ORDER_TRANSITION",
                    "Cannot move order from " + order.getStatus() + " to " + target + ".");
        }
        order.setStatus(target);
        return OrderResponse.from(orderRepository.save(order));
    }

    private Order findOrThrow(UUID id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Order not found."));
    }
}
