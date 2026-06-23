package com.eximplatform.messaging.repository;

import com.eximplatform.messaging.domain.Conversation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.UUID;

public interface ConversationRepository extends MongoRepository<Conversation, UUID> {
    /** Conversations the given company participates in (matches membership of the array field). */
    Page<Conversation> findByParticipantCompanyIds(UUID companyId, Pageable pageable);
}
