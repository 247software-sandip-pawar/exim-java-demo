/**
 * Verification module - company KYC (IEC / GST / RCMC / BANK document checks).
 *
 * IMPLEMENTED (Phase 2), backed by its own database {@code exim_verification}:
 *   verification/domain      Verification entity + VerificationType/VerificationStatus enums
 *   verification/repository  VerificationRepository
 *   verification/service     VerificationService (@Transactional on verificationTransactionManager)
 *   verification/controller  VerificationController
 *   verification/dto         VerificationRequest / VerificationResponse
 *
 * Endpoints: POST/GET /api/v1/companies/{companyId}/verifications,
 *            GET /api/v1/companies/{companyId}/verifications/{verificationId}
 *
 * Deferred: real file upload (a StorageService abstraction — today the client supplies a fileUrl),
 * and admin approve/reject of submissions (Phase 6).
 */
package com.eximplatform.verification;
