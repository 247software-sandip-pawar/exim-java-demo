package com.eximplatform.billing.controller;

import com.eximplatform.billing.dto.PlatformPaymentResponse;
import com.eximplatform.billing.service.PlatformPaymentService;
import com.eximplatform.common.api.ApiResponse;
import com.eximplatform.common.api.PageResponse;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/platform-payments")
public class PlatformPaymentController {

    private final PlatformPaymentService paymentService;

    public PlatformPaymentController(PlatformPaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @GetMapping("/{id}")
    public ApiResponse<PlatformPaymentResponse> get(@PathVariable UUID id) {
        return ApiResponse.ok(paymentService.get(id));
    }

    /** List platform payments; optionally filter by {@code companyId} or {@code subscriptionId}. */
    @GetMapping
    public ApiResponse<PageResponse<PlatformPaymentResponse>> list(
            @RequestParam(required = false) UUID companyId,
            @RequestParam(required = false) UUID subscriptionId,
            Pageable pageable) {
        return ApiResponse.ok(paymentService.list(companyId, subscriptionId, pageable));
    }

    @PostMapping("/{id}/pay")
    @PreAuthorize("hasAnyRole('COMPANY_ADMIN', 'PLATFORM_ADMIN')")
    public ApiResponse<PlatformPaymentResponse> pay(@PathVariable UUID id) {
        return ApiResponse.ok(paymentService.markPaid(id));
    }
}
