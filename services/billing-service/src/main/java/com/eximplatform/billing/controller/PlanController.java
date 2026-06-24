package com.eximplatform.billing.controller;

import com.eximplatform.billing.dto.PlanResponse;
import com.eximplatform.billing.service.PlanService;
import com.eximplatform.common.api.ApiResponse;
import com.eximplatform.common.api.PageResponse;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/plans")
public class PlanController {

    private final PlanService planService;

    public PlanController(PlanService planService) {
        this.planService = planService;
    }

    @GetMapping
    public ApiResponse<PageResponse<PlanResponse>> list(Pageable pageable) {
        return ApiResponse.ok(planService.list(pageable));
    }

    @GetMapping("/{id}")
    public ApiResponse<PlanResponse> get(@PathVariable UUID id) {
        return ApiResponse.ok(planService.get(id));
    }
}
