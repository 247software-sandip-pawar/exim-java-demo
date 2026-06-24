package com.eximplatform.activity.dto;

import com.eximplatform.activity.domain.ActivityEvent;

import java.time.Instant;
import java.util.UUID;

public record ActivityEventResponse(
        UUID id, String actor, String role, String method, String path, int status, Instant at) {

    public static ActivityEventResponse from(ActivityEvent e) {
        return new ActivityEventResponse(e.getId(), e.getActor(), e.getRole(), e.getMethod(),
                e.getPath(), e.getStatus(), e.getCreatedAt());
    }
}
