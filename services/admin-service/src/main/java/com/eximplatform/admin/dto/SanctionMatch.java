package com.eximplatform.admin.dto;

import com.eximplatform.admin.domain.SanctionedEntity;

import java.util.UUID;

public record SanctionMatch(UUID id, String name, String country, String programme, String reason) {

    public static SanctionMatch from(SanctionedEntity e) {
        return new SanctionMatch(e.getId(), e.getName(), e.getCountry(), e.getProgramme(), e.getReason());
    }
}
