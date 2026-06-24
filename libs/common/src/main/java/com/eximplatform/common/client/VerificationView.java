package com.eximplatform.common.client;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.UUID;

/**
 * Minimal view of a KYC verification, as seen by other services (e.g. admin approving/rejecting it).
 * Tolerant of extra fields so the verification response can evolve independently.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record VerificationView(
        UUID id,
        UUID companyId,
        String type,
        String fileUrl,
        String status,
        String reviewerNote) {
}
