package com.eximplatform.payments.repository;

import com.eximplatform.payments.domain.Transaction;
import com.eximplatform.payments.domain.TransactionStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;
import java.util.UUID;

public interface TransactionRepository extends MongoRepository<Transaction, UUID> {
    Optional<Transaction> findByOrderId(UUID orderId);
    Page<Transaction> findByStatus(TransactionStatus status, Pageable pageable);
}
