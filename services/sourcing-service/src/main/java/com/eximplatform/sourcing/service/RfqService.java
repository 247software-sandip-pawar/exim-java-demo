package com.eximplatform.sourcing.service;

import com.eximplatform.common.api.PageResponse;
import com.eximplatform.common.client.CatalogClient;
import com.eximplatform.common.client.IdentityClient;
import com.eximplatform.common.exception.NotFoundException;
import com.eximplatform.sourcing.domain.Rfq;
import com.eximplatform.sourcing.domain.RfqStatus;
import com.eximplatform.sourcing.dto.RfqMatchesResponse;
import com.eximplatform.sourcing.dto.RfqRequest;
import com.eximplatform.sourcing.dto.RfqResponse;
import com.eximplatform.sourcing.repository.RfqRepository;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Request-for-quotation management and matching.
 *
 * <p>Buyer company existence is validated against the identity service; RFQ matches are fetched
 * from the catalog service by HS code. Both are REST calls (no shared databases).
 */
@Service
@Transactional(transactionManager = "sourcingTransactionManager")
public class RfqService {

    private final RfqRepository rfqRepository;
    private final IdentityClient identityClient;
    private final CatalogClient catalogClient;

    public RfqService(RfqRepository rfqRepository,
                      IdentityClient identityClient,
                      CatalogClient catalogClient) {
        this.rfqRepository = rfqRepository;
        this.identityClient = identityClient;
        this.catalogClient = catalogClient;
    }

    public RfqResponse create(RfqRequest req) {
        if (!identityClient.companyExists(req.getBuyerCompanyId())) {
            throw new NotFoundException("Company not found.");
        }
        Rfq rfq = new Rfq();
        rfq.setBuyerCompanyId(req.getBuyerCompanyId());
        rfq.setTitle(req.getTitle());
        rfq.setDescription(req.getDescription());
        rfq.setHsCode(req.getHsCode());
        rfq.setQuantity(req.getQuantity());
        rfq.setUnit(req.getUnit());
        rfq.setTargetPrice(req.getTargetPrice());
        rfq.setCurrency(req.getCurrency());
        rfq.setStatus(RfqStatus.OPEN);
        return RfqResponse.from(rfqRepository.save(rfq));
    }

    @Transactional(transactionManager = "sourcingTransactionManager", readOnly = true)
    public RfqResponse get(UUID id) {
        return RfqResponse.from(findOrThrow(id));
    }

    @Transactional(transactionManager = "sourcingTransactionManager", readOnly = true)
    public PageResponse<RfqResponse> list(Pageable pageable) {
        return PageResponse.from(rfqRepository.findAll(pageable), RfqResponse::from);
    }

    /** Match an RFQ to catalog products sharing its HS code (cross-service call to catalog). */
    @Transactional(transactionManager = "sourcingTransactionManager", readOnly = true)
    public RfqMatchesResponse matches(UUID id) {
        Rfq rfq = findOrThrow(id);
        return RfqMatchesResponse.of(rfq.getId(), rfq.getHsCode(),
                catalogClient.findProductsByHsCode(rfq.getHsCode()));
    }

    private Rfq findOrThrow(UUID id) {
        return rfqRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("RFQ not found."));
    }
}
