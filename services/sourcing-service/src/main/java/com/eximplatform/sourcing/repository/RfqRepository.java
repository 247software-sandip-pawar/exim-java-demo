package com.eximplatform.sourcing.repository;

import com.eximplatform.sourcing.domain.Rfq;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.UUID;

public interface RfqRepository extends MongoRepository<Rfq, UUID> {
}
