package com.eximplatform.documents.controller;

import com.eximplatform.common.api.ApiResponse;
import com.eximplatform.common.api.PageResponse;
import com.eximplatform.documents.dto.DocumentResponse;
import com.eximplatform.documents.dto.GenerateDocumentRequest;
import com.eximplatform.documents.service.TradeDocumentService;
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
@RequestMapping("/api/v1/documents")
public class TradeDocumentController {

    private final TradeDocumentService documentService;

    public TradeDocumentController(TradeDocumentService documentService) {
        this.documentService = documentService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('COMPANY_ADMIN', 'COMPANY_MEMBER', 'PLATFORM_ADMIN')")
    public ApiResponse<DocumentResponse> generate(@Valid @RequestBody GenerateDocumentRequest request) {
        return ApiResponse.ok(documentService.generate(request));
    }

    @GetMapping("/{id}")
    public ApiResponse<DocumentResponse> get(@PathVariable UUID id) {
        return ApiResponse.ok(documentService.get(id));
    }

    /** List documents; optionally filter by {@code orderId}. */
    @GetMapping
    public ApiResponse<PageResponse<DocumentResponse>> list(
            @RequestParam(required = false) UUID orderId, Pageable pageable) {
        return ApiResponse.ok(documentService.list(orderId, pageable));
    }
}
