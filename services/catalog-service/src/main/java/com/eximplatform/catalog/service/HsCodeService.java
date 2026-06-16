package com.eximplatform.catalog.service;

import com.eximplatform.catalog.dto.HsCodeResponse;
import com.eximplatform.catalog.repository.HsCodeRepository;
import com.eximplatform.common.api.PageResponse;
import com.eximplatform.common.exception.NotFoundException;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(transactionManager = "catalogTransactionManager", readOnly = true)
public class HsCodeService {

    private final HsCodeRepository hsCodeRepository;

    public HsCodeService(HsCodeRepository hsCodeRepository) {
        this.hsCodeRepository = hsCodeRepository;
    }

    public PageResponse<HsCodeResponse> list(Pageable pageable) {
        return PageResponse.from(hsCodeRepository.findAll(pageable), HsCodeResponse::from);
    }

    public HsCodeResponse getByCode(String code) {
        return hsCodeRepository.findByCode(code)
                .map(HsCodeResponse::from)
                .orElseThrow(() -> new NotFoundException("HS code not found."));
    }
}
