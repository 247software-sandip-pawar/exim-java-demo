package com.eximplatform.admin.controller;

import com.eximplatform.admin.dto.AdminActionResponse;
import com.eximplatform.admin.dto.KycDecisionRequest;
import com.eximplatform.admin.dto.MetricsResponse;
import com.eximplatform.admin.dto.SanctionMatch;
import com.eximplatform.admin.dto.SanctionScreenRequest;
import com.eximplatform.admin.dto.SanctionScreenResponse;
import com.eximplatform.admin.service.AdminService;
import com.eximplatform.admin.service.SanctionsService;
import com.eximplatform.common.api.ApiResponse;
import com.eximplatform.common.api.PageResponse;
import com.eximplatform.common.client.VerificationView;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

/**
 * Platform-admin operations. Every endpoint requires the PLATFORM_ADMIN role. KYC decisions are
 * applied to the verification service over REST (carrying the admin's JWT) and audit-logged.
 */
@RestController
@RequestMapping("/api/v1/admin")
@PreAuthorize("hasRole('PLATFORM_ADMIN')")
public class AdminController {

    private final AdminService adminService;
    private final SanctionsService sanctionsService;

    public AdminController(AdminService adminService, SanctionsService sanctionsService) {
        this.adminService = adminService;
        this.sanctionsService = sanctionsService;
    }

    // --- KYC approval ---

    @PostMapping("/verifications/{id}/approve")
    public ApiResponse<VerificationView> approve(@PathVariable UUID id,
                                                 @RequestBody(required = false) KycDecisionRequest request,
                                                 Authentication auth) {
        return ApiResponse.ok(adminService.decideKyc(id, true, note(request), actor(auth)));
    }

    @PostMapping("/verifications/{id}/reject")
    public ApiResponse<VerificationView> reject(@PathVariable UUID id,
                                                @RequestBody(required = false) KycDecisionRequest request,
                                                Authentication auth) {
        return ApiResponse.ok(adminService.decideKyc(id, false, note(request), actor(auth)));
    }

    // --- Sanctions screening ---

    @PostMapping("/sanctions/screen")
    public ApiResponse<SanctionScreenResponse> screen(@Valid @RequestBody SanctionScreenRequest request,
                                                      Authentication auth) {
        return ApiResponse.ok(sanctionsService.screen(request.getName(), actor(auth)));
    }

    @GetMapping("/sanctions")
    public ApiResponse<PageResponse<SanctionMatch>> sanctions(Pageable pageable) {
        return ApiResponse.ok(sanctionsService.list(pageable));
    }

    // --- Audit log & metrics ---

    @GetMapping("/actions")
    public ApiResponse<PageResponse<AdminActionResponse>> actions(Pageable pageable) {
        return ApiResponse.ok(adminService.listActions(pageable));
    }

    @GetMapping("/metrics")
    public ApiResponse<MetricsResponse> metrics() {
        return ApiResponse.ok(adminService.metrics());
    }

    private static String note(KycDecisionRequest request) {
        return request != null ? request.getNote() : null;
    }

    private static String actor(Authentication auth) {
        return auth != null ? auth.getName() : "system";
    }
}
