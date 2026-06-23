package com.eximplatform.documents.repository;

import com.eximplatform.documents.domain.TradeDocument;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.UUID;

public interface TradeDocumentRepository extends MongoRepository<TradeDocument, UUID> {
    Page<TradeDocument> findByOrderId(UUID orderId, Pageable pageable);
}
