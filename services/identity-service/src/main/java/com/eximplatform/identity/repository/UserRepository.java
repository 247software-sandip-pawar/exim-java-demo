package com.eximplatform.identity.repository;

import com.eximplatform.identity.domain.User;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends MongoRepository<User, UUID> {
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);

    /**
     * {@code company} is a {@code @DBRef}, so existence is matched against the reference's id
     * ({@code company.$id}). Derived {@code existsByCompanyId} cannot traverse a DBRef, hence the
     * explicit query.
     */
    @Query(value = "{ 'company.$id' : ?0 }", exists = true)
    boolean existsByCompanyId(UUID companyId);
}
