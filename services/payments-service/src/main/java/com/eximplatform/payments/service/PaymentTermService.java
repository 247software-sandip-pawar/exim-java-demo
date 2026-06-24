package com.eximplatform.payments.service;

import com.eximplatform.common.api.PageResponse;
import com.eximplatform.payments.dto.PaymentTermResponse;
import com.eximplatform.payments.repository.PaymentTermRepository;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Read access to the seeded payment terms. */
@Service
@Transactional(transactionManager = "paymentsTransactionManager", readOnly = true)
public class PaymentTermService {

    private final PaymentTermRepository paymentTermRepository;

    public PaymentTermService(PaymentTermRepository paymentTermRepository) {
        this.paymentTermRepository = paymentTermRepository;
    }

    public PageResponse<PaymentTermResponse> list(Pageable pageable) {
        return PageResponse.from(paymentTermRepository.findAll(pageable), PaymentTermResponse::from);
    }
}
