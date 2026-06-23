package com.eximplatform.messaging.controller;

import com.eximplatform.common.api.ApiResponse;
import com.eximplatform.common.api.PageResponse;
import com.eximplatform.messaging.dto.ConversationRequest;
import com.eximplatform.messaging.dto.ConversationResponse;
import com.eximplatform.messaging.dto.MessageRequest;
import com.eximplatform.messaging.dto.MessageResponse;
import com.eximplatform.messaging.service.ConversationService;
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
@RequestMapping("/api/v1/conversations")
public class ConversationController {

    private final ConversationService conversationService;

    public ConversationController(ConversationService conversationService) {
        this.conversationService = conversationService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('COMPANY_ADMIN', 'COMPANY_MEMBER', 'PLATFORM_ADMIN')")
    public ApiResponse<ConversationResponse> create(@Valid @RequestBody ConversationRequest request) {
        return ApiResponse.ok(conversationService.create(request));
    }

    @GetMapping("/{id}")
    public ApiResponse<ConversationResponse> get(@PathVariable UUID id) {
        return ApiResponse.ok(conversationService.get(id));
    }

    /** List conversations; optionally filter to those a {@code participantCompanyId} is part of. */
    @GetMapping
    public ApiResponse<PageResponse<ConversationResponse>> list(
            @RequestParam(required = false) UUID participantCompanyId, Pageable pageable) {
        return ApiResponse.ok(conversationService.list(participantCompanyId, pageable));
    }

    @PostMapping("/{id}/messages")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('COMPANY_ADMIN', 'COMPANY_MEMBER', 'PLATFORM_ADMIN')")
    public ApiResponse<MessageResponse> postMessage(@PathVariable UUID id,
                                                    @Valid @RequestBody MessageRequest request) {
        return ApiResponse.ok(conversationService.postMessage(id, request));
    }

    @GetMapping("/{id}/messages")
    public ApiResponse<PageResponse<MessageResponse>> listMessages(@PathVariable UUID id, Pageable pageable) {
        return ApiResponse.ok(conversationService.listMessages(id, pageable));
    }
}
