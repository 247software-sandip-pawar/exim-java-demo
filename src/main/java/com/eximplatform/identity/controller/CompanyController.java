package com.eximplatform.identity.controller;

import com.eximplatform.common.api.ApiResponse;
import com.eximplatform.common.api.PageResponse;
import com.eximplatform.identity.dto.CompanyRequest;
import com.eximplatform.identity.dto.CompanyResponse;
import com.eximplatform.identity.service.CompanyService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/companies")
public class CompanyController {

    private final CompanyService companyService;

    public CompanyController(CompanyService companyService) {
        this.companyService = companyService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('PLATFORM_ADMIN')")
    public ApiResponse<CompanyResponse> create(@Valid @RequestBody CompanyRequest request) {
        return ApiResponse.ok(companyService.create(request));
    }

    /** Readable by any authenticated user (company lookup/discovery). */
    @GetMapping("/{id}")
    public ApiResponse<CompanyResponse> get(@PathVariable UUID id) {
        return ApiResponse.ok(companyService.get(id));
    }

    /** Readable by any authenticated user (company lookup/discovery). */
    @GetMapping
    public ApiResponse<PageResponse<CompanyResponse>> list(Pageable pageable) {
        return ApiResponse.ok(companyService.list(pageable));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('PLATFORM_ADMIN')")
    public ApiResponse<CompanyResponse> update(@PathVariable UUID id,
                                               @Valid @RequestBody CompanyRequest request) {
        return ApiResponse.ok(companyService.update(id, request));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('PLATFORM_ADMIN')")
    public void delete(@PathVariable UUID id) {
        companyService.delete(id);
    }
}
