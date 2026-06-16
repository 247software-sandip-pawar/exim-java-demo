package com.eximplatform.verification.domain;

/**
 * Lifecycle of a submitted verification. New submissions start {@code PENDING};
 * admin approval/rejection (moving to {@code APPROVED}/{@code REJECTED}) arrives in Phase 6.
 */
public enum VerificationStatus {
    PENDING, APPROVED, REJECTED
}
