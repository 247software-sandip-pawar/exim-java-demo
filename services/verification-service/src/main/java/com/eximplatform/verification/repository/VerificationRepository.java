package com.eximplatform.verification.repository;

import com.eximplatform.verification.domain.Verification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface VerificationRepository extends JpaRepository<Verification, UUID> {

    Page<Verification> findByCompanyId(UUID companyId, Pageable pageable);

    Optional<Verification> findByIdAndCompanyId(UUID id, UUID companyId);
}
