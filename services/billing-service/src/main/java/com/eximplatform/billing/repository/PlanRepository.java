package com.eximplatform.billing.repository;

import com.eximplatform.billing.domain.Plan;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;
import java.util.UUID;

public interface PlanRepository extends MongoRepository<Plan, UUID> {
    Optional<Plan> findByCode(String code);
}
