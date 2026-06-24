package com.eximplatform.identity.controller;

import com.eximplatform.common.api.ApiResponse;
import com.eximplatform.common.api.PageResponse;
import com.eximplatform.identity.dto.CreateStaffRequest;
import com.eximplatform.identity.dto.SetActiveRequest;
import com.eximplatform.identity.dto.UserResponse;
import com.eximplatform.identity.service.UserService;
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
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

/**
 * Platform-staff management — PLATFORM_ADMIN and SUPPORT accounts, provisioned independently of the
 * company {@code /auth/register} flow (no company attached). PLATFORM_ADMIN only.
 */
@RestController
@RequestMapping("/api/v1/staff")
@PreAuthorize("hasRole('PLATFORM_ADMIN')")
public class StaffController {

    private final UserService userService;

    public StaffController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public ApiResponse<PageResponse<UserResponse>> list(Pageable pageable) {
        return ApiResponse.ok(userService.listStaff(pageable));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<UserResponse> create(@Valid @RequestBody CreateStaffRequest request) {
        return ApiResponse.ok(userService.createStaff(request));
    }

    @PatchMapping("/{id}/active")
    public ApiResponse<UserResponse> setActive(@PathVariable UUID id,
                                               @Valid @RequestBody SetActiveRequest request) {
        return ApiResponse.ok(userService.setActive(id, request.getActive()));
    }
}
