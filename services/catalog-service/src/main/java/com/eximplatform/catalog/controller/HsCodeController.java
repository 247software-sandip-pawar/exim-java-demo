package com.eximplatform.catalog.controller;

import com.eximplatform.catalog.dto.HsCodeResponse;
import com.eximplatform.catalog.service.HsCodeService;
import com.eximplatform.common.api.ApiResponse;
import com.eximplatform.common.api.PageResponse;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/hs-codes")
public class HsCodeController {

    private final HsCodeService hsCodeService;

    public HsCodeController(HsCodeService hsCodeService) {
        this.hsCodeService = hsCodeService;
    }

    @GetMapping
    public ApiResponse<PageResponse<HsCodeResponse>> list(Pageable pageable) {
        return ApiResponse.ok(hsCodeService.list(pageable));
    }

    @GetMapping("/{code}")
    public ApiResponse<HsCodeResponse> get(@PathVariable String code) {
        return ApiResponse.ok(hsCodeService.getByCode(code));
    }
}
