package com.eximplatform.payments.controller;

import com.eximplatform.common.api.ApiResponse;
import com.eximplatform.common.api.PageResponse;
import com.eximplatform.payments.domain.TransactionStatus;
import com.eximplatform.payments.dto.InitiatePaymentRequest;
import com.eximplatform.payments.dto.TransactionResponse;
import com.eximplatform.payments.service.PaymentService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/payments")
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('COMPANY_ADMIN', 'COMPANY_MEMBER', 'PLATFORM_ADMIN')")
    public ApiResponse<TransactionResponse> initiate(@Valid @RequestBody InitiatePaymentRequest request) {
        return ApiResponse.ok(paymentService.initiate(request));
    }

    @GetMapping("/{id}")
    public ApiResponse<TransactionResponse> get(@PathVariable UUID id) {
        return ApiResponse.ok(paymentService.get(id));
    }

    /** List transactions; optionally filter by {@code status}. */
    @GetMapping
    public ApiResponse<PageResponse<TransactionResponse>> list(
            @RequestParam(required = false) TransactionStatus status, Pageable pageable) {
        return ApiResponse.ok(paymentService.list(status, pageable));
    }

    /** Fund the escrow (money received into escrow). */
    @PostMapping("/{id}/fund")
    @PreAuthorize("hasAnyRole('COMPANY_ADMIN', 'COMPANY_MEMBER', 'PLATFORM_ADMIN')")
    public ApiResponse<TransactionResponse> fund(@PathVariable UUID id) {
        return ApiResponse.ok(paymentService.fund(id));
    }

    /** Release the escrow to the seller (e.g. on delivery milestone). */
    @PostMapping("/{id}/release")
    @PreAuthorize("hasAnyRole('COMPANY_ADMIN', 'PLATFORM_ADMIN')")
    public ApiResponse<TransactionResponse> release(@PathVariable UUID id) {
        return ApiResponse.ok(paymentService.release(id));
    }

    /** Refund the escrow back to the buyer. */
    @PostMapping("/{id}/refund")
    @PreAuthorize("hasAnyRole('COMPANY_ADMIN', 'PLATFORM_ADMIN')")
    public ApiResponse<TransactionResponse> refund(@PathVariable UUID id) {
        return ApiResponse.ok(paymentService.refund(id));
    }
}
