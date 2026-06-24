package com.eximplatform.verification.service;

import com.eximplatform.common.api.PageResponse;
import com.eximplatform.common.client.IdentityClient;
import com.eximplatform.common.exception.BusinessException;
import com.eximplatform.common.exception.NotFoundException;
import com.eximplatform.verification.domain.Verification;
import com.eximplatform.verification.domain.VerificationStatus;
import com.eximplatform.verification.dto.VerificationDecisionRequest;
import com.eximplatform.verification.dto.VerificationRequest;
import com.eximplatform.verification.dto.VerificationResponse;
import com.eximplatform.verification.repository.VerificationRepository;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * KYC document submission and lookup for a company.
 *
 * <p>Bound explicitly to {@code verificationTransactionManager} (the service's
 * {@code MongoTransactionManager}, consistent with the platform convention).
 *
 * <p>Company existence is checked against the identity <em>service</em> over REST via
 * {@link IdentityClient} — verification owns no company data.
 */
@Service
@Transactional(transactionManager = "verificationTransactionManager")
public class VerificationService {

    private final VerificationRepository verificationRepository;
    private final IdentityClient identityClient;

    public VerificationService(VerificationRepository verificationRepository,
                               IdentityClient identityClient) {
        this.verificationRepository = verificationRepository;
        this.identityClient = identityClient;
    }

    public VerificationResponse submit(UUID companyId, VerificationRequest req) {
        requireCompany(companyId);
        Verification v = new Verification();
        v.setCompanyId(companyId);
        v.setType(req.getType());
        v.setFileUrl(req.getFileUrl());
        v.setStatus(VerificationStatus.PENDING);
        return VerificationResponse.from(verificationRepository.save(v));
    }

    @Transactional(transactionManager = "verificationTransactionManager", readOnly = true)
    public PageResponse<VerificationResponse> list(UUID companyId, Pageable pageable) {
        requireCompany(companyId);
        return PageResponse.from(
                verificationRepository.findByCompanyId(companyId, pageable), VerificationResponse::from);
    }

    @Transactional(transactionManager = "verificationTransactionManager", readOnly = true)
    public VerificationResponse get(UUID companyId, UUID verificationId) {
        return verificationRepository.findByIdAndCompanyId(verificationId, companyId)
                .map(VerificationResponse::from)
                .orElseThrow(() -> new NotFoundException("Verification not found."));
    }

    /** Look up a verification by id alone (used by the admin decision flow). */
    @Transactional(transactionManager = "verificationTransactionManager", readOnly = true)
    public VerificationResponse getById(UUID verificationId) {
        return verificationRepository.findById(verificationId)
                .map(VerificationResponse::from)
                .orElseThrow(() -> new NotFoundException("Verification not found."));
    }

    /**
     * Admin approves or rejects a submitted verification. Only a PENDING submission can be decided;
     * the target must be APPROVED or REJECTED. MongoDB has no dirty-checking, so the change is saved.
     */
    public VerificationResponse decide(UUID verificationId, VerificationDecisionRequest req) {
        if (req.getStatus() == VerificationStatus.PENDING) {
            throw new BusinessException("INVALID_DECISION", "Decision must be APPROVED or REJECTED.");
        }
        Verification v = verificationRepository.findById(verificationId)
                .orElseThrow(() -> new NotFoundException("Verification not found."));
        if (v.getStatus() != VerificationStatus.PENDING) {
            throw new BusinessException("ALREADY_DECIDED",
                    "Verification is " + v.getStatus() + "; only a PENDING submission can be decided.");
        }
        v.setStatus(req.getStatus());
        v.setReviewerNote(req.getReviewerNote());
        return VerificationResponse.from(verificationRepository.save(v));
    }

    private void requireCompany(UUID companyId) {
        if (!identityClient.companyExists(companyId)) {
            throw new NotFoundException("Company not found.");
        }
    }
}
