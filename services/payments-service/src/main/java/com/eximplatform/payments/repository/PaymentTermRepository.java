package com.eximplatform.payments.repository;

import com.eximplatform.payments.domain.PaymentTerm;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.UUID;

public interface PaymentTermRepository extends MongoRepository<PaymentTerm, UUID> {
    boolean existsByCode(String code);
}
