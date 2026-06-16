package com.eximplatform.verification.service;

import com.eximplatform.common.api.PageResponse;
import com.eximplatform.common.exception.NotFoundException;
import com.eximplatform.identity.repository.CompanyRepository;
import com.eximplatform.verification.domain.Verification;
import com.eximplatform.verification.domain.VerificationStatus;
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
 * <p>Bound explicitly to {@code verificationTransactionManager} — the identity module is
 * {@code @Primary}, so an unqualified {@code @Transactional} would resolve to the wrong manager.
 *
 * <p>Company existence is checked against the identity module's repository. Today that is a local
 * call; once verification is extracted into its own service this becomes a remote/API lookup.
 */
@Service
@Transactional(transactionManager = "verificationTransactionManager")
public class VerificationService {

    private final VerificationRepository verificationRepository;
    private final CompanyRepository companyRepository;

    public VerificationService(VerificationRepository verificationRepository,
                               CompanyRepository companyRepository) {
        this.verificationRepository = verificationRepository;
        this.companyRepository = companyRepository;
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

    private void requireCompany(UUID companyId) {
        if (!companyRepository.existsById(companyId)) {
            throw new NotFoundException("Company not found.");
        }
    }
}
