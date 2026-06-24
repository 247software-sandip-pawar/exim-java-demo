package com.eximplatform.trust.controller;

import com.eximplatform.common.api.ApiResponse;
import com.eximplatform.common.api.PageResponse;
import com.eximplatform.trust.dto.RatingRequest;
import com.eximplatform.trust.dto.RatingResponse;
import com.eximplatform.trust.dto.RatingSummaryResponse;
import com.eximplatform.trust.service.RatingService;
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
@RequestMapping("/api/v1/ratings")
public class RatingController {

    private final RatingService ratingService;

    public RatingController(RatingService ratingService) {
        this.ratingService = ratingService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('COMPANY_ADMIN', 'COMPANY_MEMBER', 'PLATFORM_ADMIN')")
    public ApiResponse<RatingResponse> create(@Valid @RequestBody RatingRequest request) {
        return ApiResponse.ok(ratingService.create(request));
    }

    @GetMapping("/{id}")
    public ApiResponse<RatingResponse> get(@PathVariable UUID id) {
        return ApiResponse.ok(ratingService.get(id));
    }

    /** List ratings; optionally filter by {@code ratedCompanyId}. */
    @GetMapping
    public ApiResponse<PageResponse<RatingResponse>> list(
            @RequestParam(required = false) UUID ratedCompanyId, Pageable pageable) {
        return ApiResponse.ok(ratingService.list(ratedCompanyId, pageable));
    }

    /** Aggregate rating (count + average) for a company. */
    @GetMapping("/summary")
    public ApiResponse<RatingSummaryResponse> summary(@RequestParam UUID ratedCompanyId) {
        return ApiResponse.ok(ratingService.summary(ratedCompanyId));
    }
}
