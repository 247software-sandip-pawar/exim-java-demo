package com.eximplatform.quotation.controller;

import com.eximplatform.common.api.ApiResponse;
import com.eximplatform.common.api.PageResponse;
import com.eximplatform.quotation.domain.QuoteStatus;
import com.eximplatform.quotation.dto.CounterQuoteRequest;
import com.eximplatform.quotation.dto.QuoteRequest;
import com.eximplatform.quotation.dto.QuoteResponse;
import com.eximplatform.quotation.service.QuoteService;
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
@RequestMapping("/api/v1/quotes")
public class QuoteController {

    private final QuoteService quoteService;

    public QuoteController(QuoteService quoteService) {
        this.quoteService = quoteService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('COMPANY_ADMIN', 'COMPANY_MEMBER', 'PLATFORM_ADMIN')")
    public ApiResponse<QuoteResponse> submit(@Valid @RequestBody QuoteRequest request) {
        return ApiResponse.ok(quoteService.submit(request));
    }

    @GetMapping("/{id}")
    public ApiResponse<QuoteResponse> get(@PathVariable UUID id) {
        return ApiResponse.ok(quoteService.get(id));
    }

    /** List quotes; optionally filter by {@code rfqId} and/or {@code status}. */
    @GetMapping
    public ApiResponse<PageResponse<QuoteResponse>> list(
            @RequestParam(required = false) UUID rfqId,
            @RequestParam(required = false) QuoteStatus status,
            Pageable pageable) {
        return ApiResponse.ok(quoteService.list(rfqId, status, pageable));
    }

    @PostMapping("/{id}/accept")
    @PreAuthorize("hasAnyRole('COMPANY_ADMIN', 'COMPANY_MEMBER', 'PLATFORM_ADMIN')")
    public ApiResponse<QuoteResponse> accept(@PathVariable UUID id) {
        return ApiResponse.ok(quoteService.accept(id));
    }

    @PostMapping("/{id}/reject")
    @PreAuthorize("hasAnyRole('COMPANY_ADMIN', 'COMPANY_MEMBER', 'PLATFORM_ADMIN')")
    public ApiResponse<QuoteResponse> reject(@PathVariable UUID id) {
        return ApiResponse.ok(quoteService.reject(id));
    }

    @PostMapping("/{id}/counter")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('COMPANY_ADMIN', 'COMPANY_MEMBER', 'PLATFORM_ADMIN')")
    public ApiResponse<QuoteResponse> counter(@PathVariable UUID id,
                                              @Valid @RequestBody CounterQuoteRequest request) {
        return ApiResponse.ok(quoteService.counter(id, request));
    }
}
