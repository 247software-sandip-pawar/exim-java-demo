package com.eximplatform.verification.dto;

import com.eximplatform.verification.domain.VerificationStatus;
import jakarta.validation.constraints.NotNull;

/**
 * An admin's decision on a submitted verification. {@code status} must be APPROVED or REJECTED
 * (validated in the service — PENDING is not a decision).
 */
public class VerificationDecisionRequest {

    @NotNull
    private VerificationStatus status;

    private String reviewerNote;

    public VerificationStatus getStatus() { return status; }
    public void setStatus(VerificationStatus status) { this.status = status; }
    public String getReviewerNote() { return reviewerNote; }
    public void setReviewerNote(String reviewerNote) { this.reviewerNote = reviewerNote; }
}
