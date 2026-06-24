package com.eximplatform.notification.controller;

import com.eximplatform.common.api.ApiResponse;
import com.eximplatform.common.api.PageResponse;
import com.eximplatform.notification.dto.NotificationRequest;
import com.eximplatform.notification.dto.NotificationResponse;
import com.eximplatform.notification.service.NotificationService;
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
@RequestMapping("/api/v1/notifications")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('COMPANY_ADMIN', 'COMPANY_MEMBER', 'PLATFORM_ADMIN', 'SUPPORT')")
    public ApiResponse<NotificationResponse> create(@Valid @RequestBody NotificationRequest request) {
        return ApiResponse.ok(notificationService.create(request));
    }

    @GetMapping("/{id}")
    public ApiResponse<NotificationResponse> get(@PathVariable UUID id) {
        return ApiResponse.ok(notificationService.get(id));
    }

    /** List notifications; optionally filter by {@code recipientCompanyId} and {@code unread}. */
    @GetMapping
    public ApiResponse<PageResponse<NotificationResponse>> list(
            @RequestParam(required = false) UUID recipientCompanyId,
            @RequestParam(required = false) Boolean unread,
            Pageable pageable) {
        return ApiResponse.ok(notificationService.list(recipientCompanyId, unread, pageable));
    }

    @PostMapping("/{id}/read")
    public ApiResponse<NotificationResponse> markRead(@PathVariable UUID id) {
        return ApiResponse.ok(notificationService.markRead(id));
    }
}
