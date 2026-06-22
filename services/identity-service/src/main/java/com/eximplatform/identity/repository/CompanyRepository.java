package com.eximplatform.identity.repository;

import com.eximplatform.identity.domain.Company;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.UUID;

public interface CompanyRepository extends MongoRepository<Company, UUID> {
}
