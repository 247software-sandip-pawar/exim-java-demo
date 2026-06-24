package com.eximplatform.logistics.controller;

import com.eximplatform.common.api.ApiResponse;
import com.eximplatform.common.api.PageResponse;
import com.eximplatform.logistics.dto.LogisticsPartnerResponse;
import com.eximplatform.logistics.service.LogisticsPartnerService;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/logistics-partners")
public class LogisticsPartnerController {

    private final LogisticsPartnerService partnerService;

    public LogisticsPartnerController(LogisticsPartnerService partnerService) {
        this.partnerService = partnerService;
    }

    @GetMapping
    public ApiResponse<PageResponse<LogisticsPartnerResponse>> list(Pageable pageable) {
        return ApiResponse.ok(partnerService.list(pageable));
    }
}
