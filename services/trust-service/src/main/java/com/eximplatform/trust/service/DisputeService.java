package com.eximplatform.trust.service;

import com.eximplatform.common.api.PageResponse;
import com.eximplatform.common.client.IdentityClient;
import com.eximplatform.common.exception.BusinessException;
import com.eximplatform.common.exception.NotFoundException;
import com.eximplatform.trust.domain.Dispute;
import com.eximplatform.trust.domain.DisputeStatus;
import com.eximplatform.trust.dto.DisputeRequest;
import com.eximplatform.trust.dto.DisputeResponse;
import com.eximplatform.trust.dto.DisputeStatusRequest;
import com.eximplatform.trust.repository.DisputeRepository;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.UUID;

/**
 * Dispute lifecycle: open → under review → resolved/rejected (cancellable while open). Companies are
 * validated against the identity service over REST; transitions are guarded. MongoDB has no
 * dirty-checking, so updates save explicitly.
 */
@Service
@Transactional(transactionManager = "trustTransactionManager")
public class DisputeService {

    private final DisputeRepository disputeRepository;
    private final IdentityClient identityClient;

    public DisputeService(DisputeRepository disputeRepository, IdentityClient identityClient) {
        this.disputeRepository = disputeRepository;
        this.identityClient = identityClient;
    }

    public DisputeResponse create(DisputeRequest req) {
        if (!identityClient.companyExists(req.getRaisedByCompanyId())) {
            throw new NotFoundException("Raising company not found.");
        }
        if (!identityClient.companyExists(req.getAgainstCompanyId())) {
            throw new NotFoundException("Company the dispute is against not found.");
        }
        Dispute dispute = new Dispute();
        dispute.setOrderId(req.getOrderId());
        dispute.setRaisedByCompanyId(req.getRaisedByCompanyId());
        dispute.setAgainstCompanyId(req.getAgainstCompanyId());
        dispute.setReason(req.getReason());
        dispute.setDescription(req.getDescription());
        dispute.setStatus(DisputeStatus.OPEN);
        return DisputeResponse.from(disputeRepository.save(dispute));
    }

    @Transactional(transactionManager = "trustTransactionManager", readOnly = true)
    public DisputeResponse get(UUID id) {
        return DisputeResponse.from(findOrThrow(id));
    }

    @Transactional(transactionManager = "trustTransactionManager", readOnly = true)
    public PageResponse<DisputeResponse> list(DisputeStatus status, UUID orderId, Pageable pageable) {
        var page = status != null
                ? disputeRepository.findByStatus(status, pageable)
                : orderId != null
                ? disputeRepository.findByOrderId(orderId, pageable)
                : disputeRepository.findAll(pageable);
        return PageResponse.from(page, DisputeResponse::from);
    }

    public DisputeResponse updateStatus(UUID id, DisputeStatusRequest req) {
        Dispute dispute = findOrThrow(id);
        DisputeStatus target = req.getStatus();
        if (!dispute.getStatus().allowedNext().contains(target)) {
            throw new BusinessException("INVALID_DISPUTE_TRANSITION",
                    "Cannot move dispute from " + dispute.getStatus() + " to " + target + ".");
        }
        dispute.setStatus(target);
        if (StringUtils.hasText(req.getResolution())) {
            dispute.setResolution(req.getResolution());
        }
        return DisputeResponse.from(disputeRepository.save(dispute));
    }

    private Dispute findOrThrow(UUID id) {
        return disputeRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Dispute not found."));
    }
}
