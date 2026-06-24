package com.eximplatform.admin.service;

import com.eximplatform.admin.domain.AdminAction;
import com.eximplatform.admin.domain.AdminActionType;
import com.eximplatform.admin.dto.AdminActionResponse;
import com.eximplatform.admin.dto.MetricsResponse;
import com.eximplatform.admin.repository.AdminActionRepository;
import com.eximplatform.admin.repository.SanctionedEntityRepository;
import com.eximplatform.common.api.PageResponse;
import com.eximplatform.common.client.VerificationClient;
import com.eximplatform.common.client.VerificationView;
import com.eximplatform.common.exception.NotFoundException;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Platform admin operations: KYC approve/reject (applied to the verification service over REST) and
 * a read model over the audit log and basic metrics. Every KYC decision is recorded as an
 * {@link AdminAction}. MongoDB has no dirty-checking, so all writes save explicitly.
 */
@Service
@Transactional(transactionManager = "adminTransactionManager")
public class AdminService {

    private final AdminActionRepository actionRepository;
    private final SanctionedEntityRepository sanctionsRepository;
    private final VerificationClient verificationClient;

    public AdminService(AdminActionRepository actionRepository,
                        SanctionedEntityRepository sanctionsRepository,
                        VerificationClient verificationClient) {
        this.actionRepository = actionRepository;
        this.sanctionsRepository = sanctionsRepository;
        this.verificationClient = verificationClient;
    }

    /** Approve or reject a KYC verification via the verification service, recording the decision. */
    public VerificationView decideKyc(UUID verificationId, boolean approve, String note, String actor) {
        String decision = approve ? "APPROVED" : "REJECTED";
        VerificationView updated = verificationClient.decide(verificationId, decision, note)
                .orElseThrow(() -> new NotFoundException("Verification not found."));

        AdminAction action = new AdminAction();
        action.setActor(actor);
        action.setType(approve ? AdminActionType.KYC_APPROVE : AdminActionType.KYC_REJECT);
        action.setTarget(verificationId.toString());
        action.setDetail(note);
        actionRepository.save(action);

        return updated;
    }

    @Transactional(transactionManager = "adminTransactionManager", readOnly = true)
    public PageResponse<AdminActionResponse> listActions(Pageable pageable) {
        return PageResponse.from(actionRepository.findAll(pageable), AdminActionResponse::from);
    }

    @Transactional(transactionManager = "adminTransactionManager", readOnly = true)
    public MetricsResponse metrics() {
        return new MetricsResponse(
                sanctionsRepository.count(),
                actionRepository.count(),
                actionRepository.countByType(AdminActionType.KYC_APPROVE),
                actionRepository.countByType(AdminActionType.KYC_REJECT),
                actionRepository.countByType(AdminActionType.SANCTION_SCREEN));
    }
}
