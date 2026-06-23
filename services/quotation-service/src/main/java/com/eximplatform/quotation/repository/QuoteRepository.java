package com.eximplatform.quotation.repository;

import com.eximplatform.quotation.domain.Quote;
import com.eximplatform.quotation.domain.QuoteStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.UUID;

public interface QuoteRepository extends MongoRepository<Quote, UUID> {
    Page<Quote> findByRfqId(UUID rfqId, Pageable pageable);
    Page<Quote> findByStatus(QuoteStatus status, Pageable pageable);
    Page<Quote> findByRfqIdAndStatus(UUID rfqId, QuoteStatus status, Pageable pageable);
}
