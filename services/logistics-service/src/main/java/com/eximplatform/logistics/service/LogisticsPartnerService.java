package com.eximplatform.logistics.service;

import com.eximplatform.common.api.PageResponse;
import com.eximplatform.logistics.dto.LogisticsPartnerResponse;
import com.eximplatform.logistics.repository.LogisticsPartnerRepository;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Read access to the seeded logistics partners. */
@Service
@Transactional(transactionManager = "logisticsTransactionManager", readOnly = true)
public class LogisticsPartnerService {

    private final LogisticsPartnerRepository partnerRepository;

    public LogisticsPartnerService(LogisticsPartnerRepository partnerRepository) {
        this.partnerRepository = partnerRepository;
    }

    public PageResponse<LogisticsPartnerResponse> list(Pageable pageable) {
        return PageResponse.from(partnerRepository.findAll(pageable), LogisticsPartnerResponse::from);
    }
}
