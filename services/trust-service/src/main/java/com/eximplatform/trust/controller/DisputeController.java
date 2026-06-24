package com.eximplatform.trust.controller;

import com.eximplatform.common.api.ApiResponse;
import com.eximplatform.common.api.PageResponse;
import com.eximplatform.trust.domain.DisputeStatus;
import com.eximplatform.trust.dto.DisputeRequest;
import com.eximplatform.trust.dto.DisputeResponse;
import com.eximplatform.trust.dto.DisputeStatusRequest;
import com.eximplatform.trust.service.DisputeService;
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
@RequestMapping("/api/v1/disputes")
public class DisputeController {

    private final DisputeService disputeService;

    public DisputeController(DisputeService disputeService) {
        this.disputeService = disputeService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('COMPANY_ADMIN', 'COMPANY_MEMBER', 'PLATFORM_ADMIN')")
    public ApiResponse<DisputeResponse> create(@Valid @RequestBody DisputeRequest request) {
        return ApiResponse.ok(disputeService.create(request));
    }

    @GetMapping("/{id}")
    public ApiResponse<DisputeResponse> get(@PathVariable UUID id) {
        return ApiResponse.ok(disputeService.get(id));
    }

    /** List disputes; optionally filter by {@code status} or {@code orderId}. */
    @GetMapping
    public ApiResponse<PageResponse<DisputeResponse>> list(
            @RequestParam(required = false) DisputeStatus status,
            @RequestParam(required = false) UUID orderId,
            Pageable pageable) {
        return ApiResponse.ok(disputeService.list(status, orderId, pageable));
    }

    /** Advance a dispute (admin/support workflow). */
    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('PLATFORM_ADMIN', 'SUPPORT')")
    public ApiResponse<DisputeResponse> updateStatus(@PathVariable UUID id,
                                                     @Valid @RequestBody DisputeStatusRequest request) {
        return ApiResponse.ok(disputeService.updateStatus(id, request));
    }
}
