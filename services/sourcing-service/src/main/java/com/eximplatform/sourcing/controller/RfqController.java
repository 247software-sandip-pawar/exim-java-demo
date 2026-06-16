package com.eximplatform.sourcing.controller;

import com.eximplatform.common.api.ApiResponse;
import com.eximplatform.common.api.PageResponse;
import com.eximplatform.sourcing.dto.RfqMatchesResponse;
import com.eximplatform.sourcing.dto.RfqRequest;
import com.eximplatform.sourcing.dto.RfqResponse;
import com.eximplatform.sourcing.service.RfqService;
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
@RequestMapping("/api/v1/rfqs")
public class RfqController {

    private final RfqService rfqService;

    public RfqController(RfqService rfqService) {
        this.rfqService = rfqService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('COMPANY_ADMIN', 'COMPANY_MEMBER', 'PLATFORM_ADMIN')")
    public ApiResponse<RfqResponse> create(@Valid @RequestBody RfqRequest request) {
        return ApiResponse.ok(rfqService.create(request));
    }

    @GetMapping("/{id}")
    public ApiResponse<RfqResponse> get(@PathVariable UUID id) {
        return ApiResponse.ok(rfqService.get(id));
    }

    @GetMapping
    public ApiResponse<PageResponse<RfqResponse>> list(Pageable pageable) {
        return ApiResponse.ok(rfqService.list(pageable));
    }

    /** Products from the catalog service that match this RFQ's HS code. */
    @GetMapping("/{id}/matches")
    public ApiResponse<RfqMatchesResponse> matches(@PathVariable UUID id) {
        return ApiResponse.ok(rfqService.matches(id));
    }
}
