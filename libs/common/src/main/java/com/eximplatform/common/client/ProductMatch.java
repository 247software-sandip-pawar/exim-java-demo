package com.eximplatform.common.client;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Minimal view of a catalog product, as seen by other services (e.g. sourcing's RFQ matching).
 * Tolerant of extra fields so the catalog response can evolve independently.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record ProductMatch(
        UUID id,
        UUID companyId,
        String name,
        String hsCode,
        BigDecimal unitPrice,
        String currency,
        String unit,
        Integer minOrderQty) {
}
