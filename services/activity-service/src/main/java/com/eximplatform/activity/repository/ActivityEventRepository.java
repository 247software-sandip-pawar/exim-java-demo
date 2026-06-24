package com.eximplatform.activity.repository;

import com.eximplatform.activity.domain.ActivityEvent;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.UUID;

public interface ActivityEventRepository extends MongoRepository<ActivityEvent, UUID> {
    Page<ActivityEvent> findByActorContainingIgnoreCase(String actor, Pageable pageable);
}
