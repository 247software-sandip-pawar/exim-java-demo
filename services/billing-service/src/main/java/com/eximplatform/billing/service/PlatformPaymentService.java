package com.eximplatform.billing.service;

import com.eximplatform.billing.domain.PaymentStatus;
import com.eximplatform.billing.domain.PlatformPayment;
import com.eximplatform.billing.dto.PlatformPaymentResponse;
import com.eximplatform.billing.repository.PlatformPaymentRepository;
import com.eximplatform.common.api.PageResponse;
import com.eximplatform.common.exception.BusinessException;
import com.eximplatform.common.exception.NotFoundException;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/** Platform (subscription) payments: lookup, listing, and marking a pending payment as paid. */
@Service
@Transactional(transactionManager = "billingTransactionManager")
public class PlatformPaymentService {

    private final PlatformPaymentRepository paymentRepository;

    public PlatformPaymentService(PlatformPaymentRepository paymentRepository) {
        this.paymentRepository = paymentRepository;
    }

    @Transactional(transactionManager = "billingTransactionManager", readOnly = true)
    public PlatformPaymentResponse get(UUID id) {
        return PlatformPaymentResponse.from(findOrThrow(id));
    }

    @Transactional(transactionManager = "billingTransactionManager", readOnly = true)
    public PageResponse<PlatformPaymentResponse> list(UUID companyId, UUID subscriptionId, Pageable pageable) {
        var page = subscriptionId != null
                ? paymentRepository.findBySubscriptionId(subscriptionId, pageable)
                : companyId != null
                ? paymentRepository.findByCompanyId(companyId, pageable)
                : paymentRepository.findAll(pageable);
        return PageResponse.from(page, PlatformPaymentResponse::from);
    }

    /** Mark a pending payment as paid (placeholder for a billing-provider webhook callback). */
    public PlatformPaymentResponse markPaid(UUID id) {
        PlatformPayment payment = findOrThrow(id);
        if (payment.getStatus() != PaymentStatus.PENDING) {
            throw new BusinessException("NOT_PAYABLE",
                    "Payment is " + payment.getStatus() + "; only a PENDING payment can be marked paid.");
        }
        payment.setStatus(PaymentStatus.PAID);
        return PlatformPaymentResponse.from(paymentRepository.save(payment));
    }

    private PlatformPayment findOrThrow(UUID id) {
        return paymentRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Platform payment not found."));
    }
}
