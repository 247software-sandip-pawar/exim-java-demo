package com.eximplatform.verification.repository;

import com.eximplatform.verification.domain.Verification;
import com.eximplatform.verification.domain.VerificationStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;
import java.util.UUID;

public interface VerificationRepository extends MongoRepository<Verification, UUID> {

    Page<Verification> findByCompanyId(UUID companyId, Pageable pageable);

    Optional<Verification> findByIdAndCompanyId(UUID id, UUID companyId);

    /** Platform-wide review queue, optionally filtered by status. */
    Page<Verification> findByStatus(VerificationStatus status, Pageable pageable);
}
