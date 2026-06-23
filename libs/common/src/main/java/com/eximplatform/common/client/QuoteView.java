package com.eximplatform.common.client;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Minimal view of a quotation quote, as seen by other services (e.g. orders building an order from
 * an accepted quote). Tolerant of extra fields so the quotation response can evolve independently.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record QuoteView(
        UUID id,
        UUID rfqId,
        UUID sellerCompanyId,
        UUID buyerCompanyId,
        UUID productId,
        String productName,
        int quantity,
        String unit,
        BigDecimal unitPrice,
        String currency,
        String incoterm,
        String status) {
}
