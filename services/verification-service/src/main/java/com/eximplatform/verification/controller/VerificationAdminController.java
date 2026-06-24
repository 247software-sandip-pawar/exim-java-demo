package com.eximplatform.verification.controller;

import com.eximplatform.common.api.ApiResponse;
import com.eximplatform.common.api.PageResponse;
import com.eximplatform.verification.domain.VerificationStatus;
import com.eximplatform.verification.dto.VerificationDecisionRequest;
import com.eximplatform.verification.dto.VerificationResponse;
import com.eximplatform.verification.service.VerificationService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

/**
 * Flat (non-company-scoped) verification endpoints used by platform admins and the admin service:
 * look up a verification by id and record an approve/reject decision.
 */
@RestController
@RequestMapping("/api/v1/verifications")
public class VerificationAdminController {

    private final VerificationService verificationService;

    public VerificationAdminController(VerificationService verificationService) {
        this.verificationService = verificationService;
    }

    /** Platform review queue — all submitted verifications, optionally filtered by {@code status}. */
    @GetMapping
    @PreAuthorize("hasAnyRole('PLATFORM_ADMIN', 'SUPPORT')")
    public ApiResponse<PageResponse<VerificationResponse>> list(
            @RequestParam(required = false) VerificationStatus status, Pageable pageable) {
        return ApiResponse.ok(verificationService.listAll(status, pageable));
    }

    @GetMapping("/{id}")
    public ApiResponse<VerificationResponse> get(@PathVariable UUID id) {
        return ApiResponse.ok(verificationService.getById(id));
    }

    @PatchMapping("/{id}/decision")
    @PreAuthorize("hasRole('PLATFORM_ADMIN')")
    public ApiResponse<VerificationResponse> decide(@PathVariable UUID id,
                                                    @Valid @RequestBody VerificationDecisionRequest request) {
        return ApiResponse.ok(verificationService.decide(id, request));
    }
}
