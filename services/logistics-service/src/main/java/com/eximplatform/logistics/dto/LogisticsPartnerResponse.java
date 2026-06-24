package com.eximplatform.logistics.dto;

import com.eximplatform.logistics.domain.LogisticsPartner;

import java.util.UUID;

public record LogisticsPartnerResponse(UUID id, String code, String name, String mode, boolean active) {

    public static LogisticsPartnerResponse from(LogisticsPartner p) {
        return new LogisticsPartnerResponse(p.getId(), p.getCode(), p.getName(),
                p.getMode() != null ? p.getMode().name() : null, p.isActive());
    }
}
