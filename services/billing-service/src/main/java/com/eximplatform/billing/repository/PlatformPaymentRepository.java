package com.eximplatform.billing.repository;

import com.eximplatform.billing.domain.PlatformPayment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.UUID;

public interface PlatformPaymentRepository extends MongoRepository<PlatformPayment, UUID> {
    Page<PlatformPayment> findByCompanyId(UUID companyId, Pageable pageable);
    Page<PlatformPayment> findBySubscriptionId(UUID subscriptionId, Pageable pageable);
}
