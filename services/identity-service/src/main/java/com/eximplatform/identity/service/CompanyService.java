package com.eximplatform.identity.service;

import com.eximplatform.common.api.PageResponse;
import com.eximplatform.common.exception.BusinessException;
import com.eximplatform.common.exception.NotFoundException;
import com.eximplatform.identity.domain.Company;
import com.eximplatform.identity.dto.CompanyRequest;
import com.eximplatform.identity.dto.CompanyResponse;
import com.eximplatform.identity.repository.CompanyRepository;
import com.eximplatform.identity.repository.UserRepository;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * CRUD for companies. Entities are mapped to DTOs inside the transaction so callers never
 * receive managed entities (open-in-view is disabled).
 */
@Service
public class CompanyService {

    private final CompanyRepository companyRepository;
    private final UserRepository userRepository;

    public CompanyService(CompanyRepository companyRepository, UserRepository userRepository) {
        this.companyRepository = companyRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public CompanyResponse create(CompanyRequest req) {
        Company company = new Company();
        company.setName(req.getName());
        company.setType(req.getType());
        company.setCountry(req.getCountry());
        company.setIecCode(req.getIecCode());
        company.setGstin(req.getGstin());
        company.setVerified(Boolean.TRUE.equals(req.getVerified()));
        return CompanyResponse.from(companyRepository.save(company));
    }

    @Transactional(readOnly = true)
    public CompanyResponse get(UUID id) {
        return CompanyResponse.from(findOrThrow(id));
    }

    @Transactional(readOnly = true)
    public PageResponse<CompanyResponse> list(Pageable pageable) {
        return PageResponse.from(companyRepository.findAll(pageable), CompanyResponse::from);
    }

    @Transactional
    public CompanyResponse update(UUID id, CompanyRequest req) {
        Company company = findOrThrow(id);
        company.setName(req.getName());
        company.setType(req.getType());
        company.setCountry(req.getCountry());
        company.setIecCode(req.getIecCode());
        company.setGstin(req.getGstin());
        if (req.getVerified() != null) {
            company.setVerified(req.getVerified());
        }
        // MongoDB has no dirty-checking; the mutated document must be saved explicitly.
        companyRepository.save(company);
        return CompanyResponse.from(company);
    }

    @Transactional
    public void delete(UUID id) {
        Company company = findOrThrow(id);
        if (userRepository.existsByCompanyId(id)) {
            throw new BusinessException("COMPANY_HAS_USERS",
                    "Cannot delete a company that still has users.");
        }
        companyRepository.delete(company);
    }

    private Company findOrThrow(UUID id) {
        return companyRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Company not found."));
    }
}
