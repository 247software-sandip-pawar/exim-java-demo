package com.eximplatform.payments.service;

import com.eximplatform.common.client.OrderView;
import com.eximplatform.common.client.OrdersClient;
import com.eximplatform.common.exception.BusinessException;
import com.eximplatform.common.exception.NotFoundException;
import com.eximplatform.payments.domain.PaymentMethod;
import com.eximplatform.payments.domain.Transaction;
import com.eximplatform.payments.domain.TransactionStatus;
import com.eximplatform.payments.dto.InitiatePaymentRequest;
import com.eximplatform.payments.dto.TransactionResponse;
import com.eximplatform.payments.repository.PaymentTermRepository;
import com.eximplatform.payments.repository.TransactionRepository;
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
class PaymentServiceTest {

    @Mock TransactionRepository transactionRepository;
    @Mock PaymentTermRepository paymentTermRepository;
    @Mock OrdersClient ordersClient;

    @InjectMocks PaymentService paymentService;

    private OrderView order() {
        return new OrderView(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(),
                UUID.randomUUID(), new BigDecimal("6250.00"), "USD", "CONFIRMED");
    }

    private InitiatePaymentRequest request(UUID orderId) {
        InitiatePaymentRequest req = new InitiatePaymentRequest();
        req.setOrderId(orderId);
        req.setMethod(PaymentMethod.ESCROW);
        return req;
    }

    @Test
    void initiate_snapshotsAmountAndPartiesFromOrder() {
        UUID orderId = UUID.randomUUID();
        OrderView order = order();
        when(transactionRepository.findByOrderId(orderId)).thenReturn(Optional.empty());
        when(ordersClient.getOrder(orderId)).thenReturn(Optional.of(order));
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(inv -> inv.getArgument(0));

        TransactionResponse response = paymentService.initiate(request(orderId));

        assertThat(response.getStatus()).isEqualTo("INITIATED");
        assertThat(response.getAmount()).isEqualByComparingTo("6250.00");
        assertThat(response.getPayerCompanyId()).isEqualTo(order.buyerCompanyId());
        assertThat(response.getPayeeCompanyId()).isEqualTo(order.sellerCompanyId());
    }

    @Test
    void initiate_isIdempotentPerOrder() {
        UUID orderId = UUID.randomUUID();
        Transaction existing = new Transaction();
        existing.setOrderId(orderId);
        existing.setStatus(TransactionStatus.FUNDED);
        when(transactionRepository.findByOrderId(orderId)).thenReturn(Optional.of(existing));

        TransactionResponse response = paymentService.initiate(request(orderId));

        assertThat(response.getStatus()).isEqualTo("FUNDED");
        verify(ordersClient, never()).getOrder(any());
        verify(transactionRepository, never()).save(any());
    }

    @Test
    void initiate_failsWhenOrderMissing() {
        UUID orderId = UUID.randomUUID();
        when(transactionRepository.findByOrderId(orderId)).thenReturn(Optional.empty());
        when(ordersClient.getOrder(orderId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> paymentService.initiate(request(orderId)))
                .isInstanceOf(NotFoundException.class);

        verify(transactionRepository, never()).save(any());
    }

    @Test
    void release_requiresFundedFirst() {
        UUID id = UUID.randomUUID();
        Transaction tx = new Transaction();
        tx.setId(id);
        tx.setStatus(TransactionStatus.INITIATED);     // not yet funded
        when(transactionRepository.findById(id)).thenReturn(Optional.of(tx));

        assertThatThrownBy(() -> paymentService.release(id))
                .isInstanceOf(BusinessException.class);

        verify(transactionRepository, never()).save(any());
    }

    @Test
    void fundThenRelease_walksTheHappyPath() {
        UUID id = UUID.randomUUID();
        Transaction tx = new Transaction();
        tx.setId(id);
        tx.setStatus(TransactionStatus.INITIATED);
        when(transactionRepository.findById(id)).thenReturn(Optional.of(tx));
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(inv -> inv.getArgument(0));

        assertThat(paymentService.fund(id).getStatus()).isEqualTo("FUNDED");
        assertThat(paymentService.release(id).getStatus()).isEqualTo("RELEASED");
    }
}
