package com.eximplatform.sourcing.repository;

import com.eximplatform.sourcing.domain.Rfq;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface RfqRepository extends JpaRepository<Rfq, UUID> {
}
