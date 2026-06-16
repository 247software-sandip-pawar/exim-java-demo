package com.eximplatform.verification.controller;

import com.eximplatform.common.api.ApiResponse;
import com.eximplatform.common.api.PageResponse;
import com.eximplatform.verification.dto.VerificationRequest;
import com.eximplatform.verification.dto.VerificationResponse;
import com.eximplatform.verification.service.VerificationService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/companies/{companyId}/verifications")
public class VerificationController {

    private final VerificationService verificationService;

    public VerificationController(VerificationService verificationService) {
        this.verificationService = verificationService;
    }

    /**
     * Submit a KYC document for a company. Restricted to company/platform admins; per-company
     * ownership scoping (a COMPANY_ADMIN may only submit for their own company) arrives with the
     * cross-cutting ownership checks (see EXECUTION_PLAN.md).
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('COMPANY_ADMIN', 'PLATFORM_ADMIN')")
    public ApiResponse<VerificationResponse> submit(@PathVariable UUID companyId,
                                                    @Valid @RequestBody VerificationRequest request) {
        return ApiResponse.ok(verificationService.submit(companyId, request));
    }

    @GetMapping
    public ApiResponse<PageResponse<VerificationResponse>> list(@PathVariable UUID companyId,
                                                                Pageable pageable) {
        return ApiResponse.ok(verificationService.list(companyId, pageable));
    }

    @GetMapping("/{verificationId}")
    public ApiResponse<VerificationResponse> get(@PathVariable UUID companyId,
                                                 @PathVariable UUID verificationId) {
        return ApiResponse.ok(verificationService.get(companyId, verificationId));
    }
}
