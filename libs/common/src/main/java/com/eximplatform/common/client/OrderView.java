package com.eximplatform.common.client;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Minimal view of an order, as seen by other services (e.g. documents generating a trade document
 * for an order). Tolerant of extra fields so the orders response can evolve independently.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record OrderView(
        UUID id,
        UUID quoteId,
        UUID buyerCompanyId,
        UUID sellerCompanyId,
        BigDecimal totalAmount,
        String currency,
        String status) {
}
