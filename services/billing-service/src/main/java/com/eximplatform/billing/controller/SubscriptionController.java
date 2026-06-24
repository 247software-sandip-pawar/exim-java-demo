package com.eximplatform.billing.controller;

import com.eximplatform.billing.domain.SubscriptionStatus;
import com.eximplatform.billing.dto.SubscribeRequest;
import com.eximplatform.billing.dto.SubscriptionResponse;
import com.eximplatform.billing.service.SubscriptionService;
import com.eximplatform.common.api.ApiResponse;
import com.eximplatform.common.api.PageResponse;
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
@RequestMapping("/api/v1/subscriptions")
public class SubscriptionController {

    private final SubscriptionService subscriptionService;

    public SubscriptionController(SubscriptionService subscriptionService) {
        this.subscriptionService = subscriptionService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('COMPANY_ADMIN', 'PLATFORM_ADMIN')")
    public ApiResponse<SubscriptionResponse> subscribe(@Valid @RequestBody SubscribeRequest request) {
        return ApiResponse.ok(subscriptionService.subscribe(request));
    }

    @GetMapping("/{id}")
    public ApiResponse<SubscriptionResponse> get(@PathVariable UUID id) {
        return ApiResponse.ok(subscriptionService.get(id));
    }

    /** List subscriptions; optionally filter by {@code companyId} or {@code status}. */
    @GetMapping
    public ApiResponse<PageResponse<SubscriptionResponse>> list(
            @RequestParam(required = false) UUID companyId,
            @RequestParam(required = false) SubscriptionStatus status,
            Pageable pageable) {
        return ApiResponse.ok(subscriptionService.list(companyId, status, pageable));
    }

    @PostMapping("/{id}/cancel")
    @PreAuthorize("hasAnyRole('COMPANY_ADMIN', 'PLATFORM_ADMIN')")
    public ApiResponse<SubscriptionResponse> cancel(@PathVariable UUID id) {
        return ApiResponse.ok(subscriptionService.cancel(id));
    }
}
