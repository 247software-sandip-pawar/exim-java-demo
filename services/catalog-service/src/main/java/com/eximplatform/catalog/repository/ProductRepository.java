package com.eximplatform.catalog.repository;

import com.eximplatform.catalog.domain.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ProductRepository extends JpaRepository<Product, UUID> {
    Page<Product> findByHsCode(String hsCode, Pageable pageable);
    Page<Product> findByCompanyId(UUID companyId, Pageable pageable);
}
