package com.eximplatform.admin.dto;

/**
 * Basic platform metrics computed from admin-owned data. Cross-service counts (companies, orders,
 * GMV, etc.) pulled live from every service are deferred.
 */
public record MetricsResponse(
        long sanctionedEntities,
        long totalAdminActions,
        long kycApprovals,
        long kycRejections,
        long sanctionScreenings) {
}
