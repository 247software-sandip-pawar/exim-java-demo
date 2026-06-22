package com.eximplatform.catalog.service;

import com.eximplatform.catalog.domain.Product;
import com.eximplatform.catalog.dto.ProductRequest;
import com.eximplatform.catalog.dto.ProductResponse;
import com.eximplatform.catalog.repository.HsCodeRepository;
import com.eximplatform.catalog.repository.ProductRepository;
import com.eximplatform.common.api.PageResponse;
import com.eximplatform.common.client.IdentityClient;
import com.eximplatform.common.exception.BusinessException;
import com.eximplatform.common.exception.NotFoundException;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.UUID;

@Service
@Transactional(transactionManager = "catalogTransactionManager")
public class ProductService {

    private final ProductRepository productRepository;
    private final HsCodeRepository hsCodeRepository;
    private final IdentityClient identityClient;

    public ProductService(ProductRepository productRepository,
                          HsCodeRepository hsCodeRepository,
                          IdentityClient identityClient) {
        this.productRepository = productRepository;
        this.hsCodeRepository = hsCodeRepository;
        this.identityClient = identityClient;
    }

    public ProductResponse create(ProductRequest req) {
        if (!identityClient.companyExists(req.getCompanyId())) {
            throw new NotFoundException("Company not found.");
        }
        requireHsCode(req.getHsCode());
        Product p = new Product();
        p.setCompanyId(req.getCompanyId());
        apply(p, req);
        p.setActive(req.getActive() == null || req.getActive());
        return ProductResponse.from(productRepository.save(p));
    }

    @Transactional(transactionManager = "catalogTransactionManager", readOnly = true)
    public ProductResponse get(UUID id) {
        return ProductResponse.from(findOrThrow(id));
    }

    @Transactional(transactionManager = "catalogTransactionManager", readOnly = true)
    public PageResponse<ProductResponse> list(String hsCode, Pageable pageable) {
        var page = StringUtils.hasText(hsCode)
                ? productRepository.findByHsCode(hsCode, pageable)
                : productRepository.findAll(pageable);
        return PageResponse.from(page, ProductResponse::from);
    }

    public ProductResponse update(UUID id, ProductRequest req) {
        Product p = findOrThrow(id);
        requireHsCode(req.getHsCode());
        apply(p, req);
        if (req.getActive() != null) {
            p.setActive(req.getActive());
        }
        // companyId (ownership) is immutable on update.
        // MongoDB has no dirty-checking; the mutated document must be saved explicitly.
        productRepository.save(p);
        return ProductResponse.from(p);
    }

    public void delete(UUID id) {
        productRepository.delete(findOrThrow(id));
    }

    private void apply(Product p, ProductRequest req) {
        p.setName(req.getName());
        p.setDescription(req.getDescription());
        p.setHsCode(req.getHsCode());
        p.setUnitPrice(req.getUnitPrice());
        p.setCurrency(req.getCurrency());
        p.setUnit(req.getUnit());
        p.setMinOrderQty(req.getMinOrderQty());
    }

    private void requireHsCode(String hsCode) {
        if (!hsCodeRepository.existsByCode(hsCode)) {
            throw new BusinessException("UNKNOWN_HS_CODE", "Unknown HS code: " + hsCode);
        }
    }

    private Product findOrThrow(UUID id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Product not found."));
    }
}
