package com.eximplatform.billing.dto;

import com.eximplatform.billing.domain.Plan;

import java.math.BigDecimal;
import java.util.UUID;

public record PlanResponse(
        UUID id, String code, String name, String description,
        BigDecimal priceMonthly, String currency, int maxProducts, int maxRfqs, boolean active) {

    public static PlanResponse from(Plan p) {
        return new PlanResponse(p.getId(), p.getCode(), p.getName(), p.getDescription(),
                p.getPriceMonthly(), p.getCurrency(), p.getMaxProducts(), p.getMaxRfqs(), p.isActive());
    }
}
