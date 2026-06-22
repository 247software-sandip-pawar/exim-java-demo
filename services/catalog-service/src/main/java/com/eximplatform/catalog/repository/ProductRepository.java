package com.eximplatform.catalog.repository;

import com.eximplatform.catalog.domain.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.UUID;

public interface ProductRepository extends MongoRepository<Product, UUID> {
    Page<Product> findByHsCode(String hsCode, Pageable pageable);
    Page<Product> findByCompanyId(UUID companyId, Pageable pageable);
}
