package com.eximplatform.admin.repository;

import com.eximplatform.admin.domain.SanctionedEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.UUID;

public interface SanctionedEntityRepository extends MongoRepository<SanctionedEntity, UUID> {
    /** Case-insensitive substring match — the basis of name screening. */
    List<SanctionedEntity> findByNameContainingIgnoreCase(String name);

    Page<SanctionedEntity> findAll(Pageable pageable);
}
