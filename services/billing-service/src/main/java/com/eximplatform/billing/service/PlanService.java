package com.eximplatform.billing.service;

import com.eximplatform.billing.dto.PlanResponse;
import com.eximplatform.billing.repository.PlanRepository;
import com.eximplatform.common.api.PageResponse;
import com.eximplatform.common.exception.NotFoundException;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/** Read access to the seeded subscription plans. */
@Service
@Transactional(transactionManager = "billingTransactionManager", readOnly = true)
public class PlanService {

    private final PlanRepository planRepository;

    public PlanService(PlanRepository planRepository) {
        this.planRepository = planRepository;
    }

    public PageResponse<PlanResponse> list(Pageable pageable) {
        return PageResponse.from(planRepository.findAll(pageable), PlanResponse::from);
    }

    public PlanResponse get(UUID id) {
        return planRepository.findById(id)
                .map(PlanResponse::from)
                .orElseThrow(() -> new NotFoundException("Plan not found."));
    }
}
