package com.eximplatform.verification.dto;

import com.eximplatform.verification.domain.Verification;

import java.time.Instant;
import java.util.UUID;

public class VerificationResponse {

    private UUID id;
    private UUID companyId;
    private String type;
    private String fileUrl;
    private String status;
    private String reviewerNote;
    private Instant createdAt;
    private Instant updatedAt;

    public static VerificationResponse from(Verification v) {
        VerificationResponse r = new VerificationResponse();
        r.id = v.getId();
        r.companyId = v.getCompanyId();
        r.type = v.getType() != null ? v.getType().name() : null;
        r.fileUrl = v.getFileUrl();
        r.status = v.getStatus() != null ? v.getStatus().name() : null;
        r.reviewerNote = v.getReviewerNote();
        r.createdAt = v.getCreatedAt();
        r.updatedAt = v.getUpdatedAt();
        return r;
    }

    public UUID getId() { return id; }
    public UUID getCompanyId() { return companyId; }
    public String getType() { return type; }
    public String getFileUrl() { return fileUrl; }
    public String getStatus() { return status; }
    public String getReviewerNote() { return reviewerNote; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
}
