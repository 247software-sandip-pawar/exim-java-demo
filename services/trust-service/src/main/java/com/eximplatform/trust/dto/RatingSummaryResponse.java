package com.eximplatform.trust.dto;

import java.util.UUID;

/** Aggregate rating for a company: how many ratings and their average score. */
public record RatingSummaryResponse(UUID ratedCompanyId, long count, double averageScore) {
}
