package com.eximplatform.catalog.repository;

import com.eximplatform.catalog.domain.HsCode;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface HsCodeRepository extends JpaRepository<HsCode, UUID> {
    Optional<HsCode> findByCode(String code);
    boolean existsByCode(String code);
}
