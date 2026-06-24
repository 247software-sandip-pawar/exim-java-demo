package com.eximplatform.payments.controller;

import com.eximplatform.common.api.ApiResponse;
import com.eximplatform.common.api.PageResponse;
import com.eximplatform.payments.dto.PaymentTermResponse;
import com.eximplatform.payments.service.PaymentTermService;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/payment-terms")
public class PaymentTermController {

    private final PaymentTermService paymentTermService;

    public PaymentTermController(PaymentTermService paymentTermService) {
        this.paymentTermService = paymentTermService;
    }

    @GetMapping
    public ApiResponse<PageResponse<PaymentTermResponse>> list(Pageable pageable) {
        return ApiResponse.ok(paymentTermService.list(pageable));
    }
}
