package com.eximplatform.catalog.repository;

import com.eximplatform.catalog.domain.HsCode;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;
import java.util.UUID;

public interface HsCodeRepository extends MongoRepository<HsCode, UUID> {
    Optional<HsCode> findByCode(String code);
    boolean existsByCode(String code);
}
