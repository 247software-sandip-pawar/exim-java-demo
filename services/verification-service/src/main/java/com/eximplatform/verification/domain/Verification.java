package com.eximplatform.verification.domain;

import com.eximplatform.common.domain.BaseEntity;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.UUID;

/**
 * A KYC document submitted by a company for verification. Owned by the verification module's
 * database; {@code companyId} links to a company in the identity module by id only (no FK).
 */
@Document(collection = "verifications")
public class Verification extends BaseEntity {

    @Indexed
    private UUID companyId;

    private VerificationType type;

    private String fileUrl;

    private VerificationStatus status = VerificationStatus.PENDING;

    private String reviewerNote;

    public UUID getCompanyId() { return companyId; }
    public void setCompanyId(UUID companyId) { this.companyId = companyId; }
    public VerificationType getType() { return type; }
    public void setType(VerificationType type) { this.type = type; }
    public String getFileUrl() { return fileUrl; }
    public void setFileUrl(String fileUrl) { this.fileUrl = fileUrl; }
    public VerificationStatus getStatus() { return status; }
    public void setStatus(VerificationStatus status) { this.status = status; }
    public String getReviewerNote() { return reviewerNote; }
    public void setReviewerNote(String reviewerNote) { this.reviewerNote = reviewerNote; }
}
