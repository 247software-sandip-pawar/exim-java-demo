package com.eximplatform.trust.dto;

import com.eximplatform.trust.domain.Rating;

import java.time.Instant;
import java.util.UUID;

public record RatingResponse(
        UUID id, UUID orderId, UUID raterCompanyId, UUID ratedCompanyId,
        int score, String comment, Instant createdAt) {

    public static RatingResponse from(Rating r) {
        return new RatingResponse(r.getId(), r.getOrderId(), r.getRaterCompanyId(),
                r.getRatedCompanyId(), r.getScore(), r.getComment(), r.getCreatedAt());
    }
}
