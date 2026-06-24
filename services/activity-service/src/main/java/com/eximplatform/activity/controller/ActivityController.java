package com.eximplatform.activity.controller;

import com.eximplatform.activity.dto.ActivityEventRequest;
import com.eximplatform.activity.dto.ActivityEventResponse;
import com.eximplatform.activity.service.ActivityService;
import com.eximplatform.common.api.ApiResponse;
import com.eximplatform.common.api.PageResponse;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/activity")
public class ActivityController {

    private final ActivityService activityService;

    public ActivityController(ActivityService activityService) {
        this.activityService = activityService;
    }

    /**
     * Records one action for the authenticated caller. Any authenticated user may record their own
     * action (the gateway calls this with the caller's token).
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<ActivityEventResponse> record(@Valid @RequestBody ActivityEventRequest request,
                                                     Authentication auth) {
        return ApiResponse.ok(activityService.record(auth.getName(), roleOf(auth), request));
    }

    /** Platform-staff-only read of the audit trail; optionally filter by {@code actor}. */
    @GetMapping
    @PreAuthorize("hasAnyRole('PLATFORM_ADMIN', 'SUPPORT')")
    public ApiResponse<PageResponse<ActivityEventResponse>> list(
            @RequestParam(required = false) String actor, Pageable pageable) {
        return ApiResponse.ok(activityService.list(actor, pageable));
    }

    private static String roleOf(Authentication auth) {
        return auth.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .findFirst()
                .map(a -> a.startsWith("ROLE_") ? a.substring(5) : a)
                .orElse(null);
    }
}
