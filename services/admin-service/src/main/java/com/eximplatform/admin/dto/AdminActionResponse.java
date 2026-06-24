package com.eximplatform.admin.dto;

import com.eximplatform.admin.domain.AdminAction;

import java.time.Instant;
import java.util.UUID;

public record AdminActionResponse(UUID id, String actor, String type, String target, String detail, Instant createdAt) {

    public static AdminActionResponse from(AdminAction a) {
        return new AdminActionResponse(a.getId(), a.getActor(),
                a.getType() != null ? a.getType().name() : null, a.getTarget(), a.getDetail(), a.getCreatedAt());
    }
}
