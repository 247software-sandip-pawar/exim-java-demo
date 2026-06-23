package com.eximplatform.orders.service;

import com.eximplatform.common.client.QuotationClient;
import com.eximplatform.common.client.QuoteView;
import com.eximplatform.common.exception.BusinessException;
import com.eximplatform.common.exception.NotFoundException;
import com.eximplatform.orders.domain.Order;
import com.eximplatform.orders.domain.OrderStatus;
import com.eximplatform.orders.dto.CreateOrderRequest;
import com.eximplatform.orders.dto.OrderResponse;
import com.eximplatform.orders.repository.OrderRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock OrderRepository orderRepository;
    @Mock QuotationClient quotationClient;

    @InjectMocks OrderService orderService;

    private QuoteView quote(String status) {
        return new QuoteView(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(),
                UUID.randomUUID(), UUID.randomUUID(), "Arabica", 500, "kg",
                new BigDecimal("12.50"), "USD", "FOB", status);
    }

    private CreateOrderRequest request(UUID quoteId) {
        CreateOrderRequest req = new CreateOrderRequest();
        req.setQuoteId(quoteId);
        return req;
    }

    @Test
    void create_buildsOrderFromAcceptedQuote() {
        UUID quoteId = UUID.randomUUID();
        when(orderRepository.findByQuoteId(quoteId)).thenReturn(Optional.empty());
        when(quotationClient.getQuote(quoteId)).thenReturn(Optional.of(quote("ACCEPTED")));
        when(orderRepository.save(any(Order.class))).thenAnswer(inv -> inv.getArgument(0));

        OrderResponse response = orderService.createFromQuote(request(quoteId));

        assertThat(response.getStatus()).isEqualTo("CREATED");
        assertThat(response.getTotalAmount()).isEqualByComparingTo("6250.00"); // 500 * 12.50
        assertThat(response.getItems()).hasSize(1);
        assertThat(response.getItems().get(0).lineTotal()).isEqualByComparingTo("6250.00");
    }

    @Test
    void create_isIdempotentPerQuote() {
        UUID quoteId = UUID.randomUUID();
        Order existing = new Order();
        existing.setQuoteId(quoteId);
        existing.setStatus(OrderStatus.CONFIRMED);
        when(orderRepository.findByQuoteId(quoteId)).thenReturn(Optional.of(existing));

        OrderResponse response = orderService.createFromQuote(request(quoteId));

        assertThat(response.getStatus()).isEqualTo("CONFIRMED");
        verify(quotationClient, never()).getQuote(any());
        verify(orderRepository, never()).save(any());
    }

    @Test
    void create_failsWhenQuoteNotAccepted() {
        UUID quoteId = UUID.randomUUID();
        when(orderRepository.findByQuoteId(quoteId)).thenReturn(Optional.empty());
        when(quotationClient.getQuote(quoteId)).thenReturn(Optional.of(quote("SUBMITTED")));

        assertThatThrownBy(() -> orderService.createFromQuote(request(quoteId)))
                .isInstanceOf(BusinessException.class);

        verify(orderRepository, never()).save(any());
    }

    @Test
    void create_failsWhenQuoteMissing() {
        UUID quoteId = UUID.randomUUID();
        when(orderRepository.findByQuoteId(quoteId)).thenReturn(Optional.empty());
        when(quotationClient.getQuote(quoteId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> orderService.createFromQuote(request(quoteId)))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void updateStatus_allowsValidTransition() {
        UUID id = UUID.randomUUID();
        Order order = new Order();
        order.setId(id);
        order.setStatus(OrderStatus.CREATED);
        when(orderRepository.findById(id)).thenReturn(Optional.of(order));
        when(orderRepository.save(any(Order.class))).thenAnswer(inv -> inv.getArgument(0));

        OrderResponse response = orderService.updateStatus(id, OrderStatus.CONFIRMED);

        assertThat(response.getStatus()).isEqualTo("CONFIRMED");
    }

    @Test
    void updateStatus_rejectsInvalidTransition() {
        UUID id = UUID.randomUUID();
        Order order = new Order();
        order.setId(id);
        order.setStatus(OrderStatus.CREATED);
        when(orderRepository.findById(id)).thenReturn(Optional.of(order));

        assertThatThrownBy(() -> orderService.updateStatus(id, OrderStatus.DELIVERED))
                .isInstanceOf(BusinessException.class);

        verify(orderRepository, never()).save(any());
    }
}
