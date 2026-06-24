package com.eximplatform.trust.repository;

import com.eximplatform.trust.domain.Rating;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.UUID;

public interface RatingRepository extends MongoRepository<Rating, UUID> {
    Page<Rating> findByRatedCompanyId(UUID ratedCompanyId, Pageable pageable);
    List<Rating> findByRatedCompanyId(UUID ratedCompanyId);
}
