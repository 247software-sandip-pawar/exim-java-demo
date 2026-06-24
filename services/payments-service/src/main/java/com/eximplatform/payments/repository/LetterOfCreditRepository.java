package com.eximplatform.payments.repository;

import com.eximplatform.payments.domain.LetterOfCredit;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.UUID;

public interface LetterOfCreditRepository extends MongoRepository<LetterOfCredit, UUID> {
    Page<LetterOfCredit> findByOrderId(UUID orderId, Pageable pageable);
}
