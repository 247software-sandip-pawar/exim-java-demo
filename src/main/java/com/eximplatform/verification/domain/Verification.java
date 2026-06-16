package com.eximplatform.verification.domain;

import com.eximplatform.common.domain.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;

import java.util.UUID;

/**
 * A KYC document submitted by a company for verification. Owned by the verification module's
 * database; {@code companyId} links to a company in the identity module by id only (no FK).
 */
@Entity
@Table(name = "verifications")
public class Verification extends BaseEntity {

    @Column(name = "company_id", nullable = false)
    private UUID companyId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private VerificationType type;

    @Column(name = "file_url", nullable = false, length = 1024)
    private String fileUrl;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private VerificationStatus status = VerificationStatus.PENDING;

    @Column(name = "reviewer_note", length = 1024)
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
