package com.eximplatform.payments.service;

import com.eximplatform.common.api.PageResponse;
import com.eximplatform.common.client.IdentityClient;
import com.eximplatform.common.exception.BusinessException;
import com.eximplatform.common.exception.NotFoundException;
import com.eximplatform.payments.domain.LcStatus;
import com.eximplatform.payments.domain.LetterOfCredit;
import com.eximplatform.payments.dto.LetterOfCreditRequest;
import com.eximplatform.payments.dto.LetterOfCreditResponse;
import com.eximplatform.payments.repository.LetterOfCreditRepository;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Letter-of-credit lifecycle: draft → issued → (confirmed) → settled. Applicant and beneficiary
 * companies are validated against the identity service over REST. State changes are guarded; MongoDB
 * has no dirty-checking, so updates call save explicitly.
 */
@Service
@Transactional(transactionManager = "paymentsTransactionManager")
public class LetterOfCreditService {

    private final LetterOfCreditRepository lcRepository;
    private final IdentityClient identityClient;

    public LetterOfCreditService(LetterOfCreditRepository lcRepository, IdentityClient identityClient) {
        this.lcRepository = lcRepository;
        this.identityClient = identityClient;
    }

    public LetterOfCreditResponse create(LetterOfCreditRequest req) {
        if (!identityClient.companyExists(req.getApplicantCompanyId())) {
            throw new NotFoundException("Applicant company not found.");
        }
        if (!identityClient.companyExists(req.getBeneficiaryCompanyId())) {
            throw new NotFoundException("Beneficiary company not found.");
        }
        LetterOfCredit lc = new LetterOfCredit();
        lc.setOrderId(req.getOrderId());
        lc.setApplicantCompanyId(req.getApplicantCompanyId());
        lc.setBeneficiaryCompanyId(req.getBeneficiaryCompanyId());
        lc.setIssuingBank(req.getIssuingBank());
        lc.setAdvisingBank(req.getAdvisingBank());
        lc.setAmount(req.getAmount());
        lc.setCurrency(req.getCurrency());
        lc.setExpiryDate(req.getExpiryDate());
        lc.setStatus(LcStatus.DRAFT);
        // LC number derived from the app-assigned id (set in BaseEntity).
        lc.setLcNumber("LC-" + lc.getId().toString().substring(0, 10).toUpperCase());
        return LetterOfCreditResponse.from(lcRepository.save(lc));
    }

    @Transactional(transactionManager = "paymentsTransactionManager", readOnly = true)
    public LetterOfCreditResponse get(UUID id) {
        return LetterOfCreditResponse.from(findOrThrow(id));
    }

    @Transactional(transactionManager = "paymentsTransactionManager", readOnly = true)
    public PageResponse<LetterOfCreditResponse> list(UUID orderId, Pageable pageable) {
        var page = orderId != null
                ? lcRepository.findByOrderId(orderId, pageable)
                : lcRepository.findAll(pageable);
        return PageResponse.from(page, LetterOfCreditResponse::from);
    }

    public LetterOfCreditResponse updateStatus(UUID id, LcStatus target) {
        LetterOfCredit lc = findOrThrow(id);
        if (!lc.getStatus().allowedNext().contains(target)) {
            throw new BusinessException("INVALID_LC_TRANSITION",
                    "Cannot move letter of credit from " + lc.getStatus() + " to " + target + ".");
        }
        lc.setStatus(target);
        return LetterOfCreditResponse.from(lcRepository.save(lc));
    }

    private LetterOfCredit findOrThrow(UUID id) {
        return lcRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Letter of credit not found."));
    }
}
