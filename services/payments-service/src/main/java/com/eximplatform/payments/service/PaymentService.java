package com.eximplatform.payments.service;

import com.eximplatform.common.api.PageResponse;
import com.eximplatform.common.client.OrderView;
import com.eximplatform.common.client.OrdersClient;
import com.eximplatform.common.exception.BusinessException;
import com.eximplatform.common.exception.NotFoundException;
import com.eximplatform.payments.domain.Transaction;
import com.eximplatform.payments.domain.TransactionStatus;
import com.eximplatform.payments.dto.InitiatePaymentRequest;
import com.eximplatform.payments.dto.TransactionResponse;
import com.eximplatform.payments.repository.PaymentTermRepository;
import com.eximplatform.payments.repository.TransactionRepository;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.UUID;

/**
 * Escrow payment lifecycle: initiate → fund → release (to seller) or refund (to buyer).
 *
 * <p>The order being paid for is read from the orders service over REST; the amount and parties are
 * snapshotted from the order, never trusted from the caller. Initiation is idempotent per order (the
 * unique {@code orderId} means a repeat returns the existing transaction). Money-state changes are
 * guarded by an explicit state machine; concurrent updates are caught by optimistic locking
 * ({@code @Version}). Real payment-partner integration is deferred (see the application javadoc).
 */
@Service
@Transactional(transactionManager = "paymentsTransactionManager")
public class PaymentService {

    private final TransactionRepository transactionRepository;
    private final PaymentTermRepository paymentTermRepository;
    private final OrdersClient ordersClient;

    public PaymentService(TransactionRepository transactionRepository,
                          PaymentTermRepository paymentTermRepository,
                          OrdersClient ordersClient) {
        this.transactionRepository = transactionRepository;
        this.paymentTermRepository = paymentTermRepository;
        this.ordersClient = ordersClient;
    }

    public TransactionResponse initiate(InitiatePaymentRequest req) {
        // Idempotency: one escrow transaction per order.
        var existing = transactionRepository.findByOrderId(req.getOrderId());
        if (existing.isPresent()) {
            return TransactionResponse.from(existing.get());
        }
        if (StringUtils.hasText(req.getPaymentTermCode())
                && !paymentTermRepository.existsByCode(req.getPaymentTermCode())) {
            throw new BusinessException("UNKNOWN_PAYMENT_TERM",
                    "Unknown payment term: " + req.getPaymentTermCode());
        }
        OrderView order = ordersClient.getOrder(req.getOrderId())
                .orElseThrow(() -> new NotFoundException("Order not found."));

        Transaction tx = new Transaction();
        tx.setOrderId(order.id());
        tx.setPayerCompanyId(order.buyerCompanyId());     // buyer pays
        tx.setPayeeCompanyId(order.sellerCompanyId());    // seller is paid
        tx.setAmount(order.totalAmount());                // snapshot from the order
        tx.setCurrency(order.currency());
        tx.setMethod(req.getMethod());
        tx.setPaymentTermCode(req.getPaymentTermCode());
        tx.setStatus(TransactionStatus.INITIATED);
        return TransactionResponse.from(transactionRepository.save(tx));
    }

    public TransactionResponse fund(UUID id) {
        return transition(id, TransactionStatus.FUNDED);
    }

    public TransactionResponse release(UUID id) {
        return transition(id, TransactionStatus.RELEASED);
    }

    public TransactionResponse refund(UUID id) {
        return transition(id, TransactionStatus.REFUNDED);
    }

    @Transactional(transactionManager = "paymentsTransactionManager", readOnly = true)
    public TransactionResponse get(UUID id) {
        return TransactionResponse.from(findOrThrow(id));
    }

    @Transactional(transactionManager = "paymentsTransactionManager", readOnly = true)
    public PageResponse<TransactionResponse> list(TransactionStatus status, Pageable pageable) {
        var page = status != null
                ? transactionRepository.findByStatus(status, pageable)
                : transactionRepository.findAll(pageable);
        return PageResponse.from(page, TransactionResponse::from);
    }

    private TransactionResponse transition(UUID id, TransactionStatus target) {
        Transaction tx = findOrThrow(id);
        if (!tx.getStatus().allowedNext().contains(target)) {
            throw new BusinessException("INVALID_PAYMENT_TRANSITION",
                    "Cannot move transaction from " + tx.getStatus() + " to " + target + ".");
        }
        tx.setStatus(target);
        return TransactionResponse.from(transactionRepository.save(tx));
    }

    private Transaction findOrThrow(UUID id) {
        return transactionRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Transaction not found."));
    }
}
