package com.eximplatform.logistics.repository;

import com.eximplatform.logistics.domain.LogisticsPartner;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.UUID;

public interface LogisticsPartnerRepository extends MongoRepository<LogisticsPartner, UUID> {
    boolean existsByCode(String code);
}
