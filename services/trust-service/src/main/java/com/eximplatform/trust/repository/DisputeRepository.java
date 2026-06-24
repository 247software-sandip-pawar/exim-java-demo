package com.eximplatform.trust.repository;

import com.eximplatform.trust.domain.Dispute;
import com.eximplatform.trust.domain.DisputeStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.UUID;

public interface DisputeRepository extends MongoRepository<Dispute, UUID> {
    Page<Dispute> findByStatus(DisputeStatus status, Pageable pageable);
    Page<Dispute> findByOrderId(UUID orderId, Pageable pageable);
}
