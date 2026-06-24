package com.eximplatform.billing.repository;

import com.eximplatform.billing.domain.Subscription;
import com.eximplatform.billing.domain.SubscriptionStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.UUID;

public interface SubscriptionRepository extends MongoRepository<Subscription, UUID> {
    Page<Subscription> findByCompanyId(UUID companyId, Pageable pageable);
    Page<Subscription> findByStatus(SubscriptionStatus status, Pageable pageable);
    boolean existsByCompanyIdAndStatus(UUID companyId, SubscriptionStatus status);
}
