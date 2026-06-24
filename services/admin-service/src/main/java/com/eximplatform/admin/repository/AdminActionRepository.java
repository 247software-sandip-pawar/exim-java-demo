package com.eximplatform.admin.repository;

import com.eximplatform.admin.domain.AdminAction;
import com.eximplatform.admin.domain.AdminActionType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.UUID;

public interface AdminActionRepository extends MongoRepository<AdminAction, UUID> {
    Page<AdminAction> findByType(AdminActionType type, Pageable pageable);
    long countByType(AdminActionType type);
}
