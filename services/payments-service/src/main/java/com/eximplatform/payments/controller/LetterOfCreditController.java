package com.eximplatform.payments.controller;

import com.eximplatform.common.api.ApiResponse;
import com.eximplatform.common.api.PageResponse;
import com.eximplatform.payments.dto.LcStatusRequest;
import com.eximplatform.payments.dto.LetterOfCreditRequest;
import com.eximplatform.payments.dto.LetterOfCreditResponse;
import com.eximplatform.payments.service.LetterOfCreditService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/letters-of-credit")
public class LetterOfCreditController {

    private final LetterOfCreditService lcService;

    public LetterOfCreditController(LetterOfCreditService lcService) {
        this.lcService = lcService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('COMPANY_ADMIN', 'COMPANY_MEMBER', 'PLATFORM_ADMIN')")
    public ApiResponse<LetterOfCreditResponse> create(@Valid @RequestBody LetterOfCreditRequest request) {
        return ApiResponse.ok(lcService.create(request));
    }

    @GetMapping("/{id}")
    public ApiResponse<LetterOfCreditResponse> get(@PathVariable UUID id) {
        return ApiResponse.ok(lcService.get(id));
    }

    /** List letters of credit; optionally filter by {@code orderId}. */
    @GetMapping
    public ApiResponse<PageResponse<LetterOfCreditResponse>> list(
            @RequestParam(required = false) UUID orderId, Pageable pageable) {
        return ApiResponse.ok(lcService.list(orderId, pageable));
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('COMPANY_ADMIN', 'PLATFORM_ADMIN')")
    public ApiResponse<LetterOfCreditResponse> updateStatus(@PathVariable UUID id,
                                                            @Valid @RequestBody LcStatusRequest request) {
        return ApiResponse.ok(lcService.updateStatus(id, request.getStatus()));
    }
}
