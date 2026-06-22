package com.eximplatform.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.MongoDatabaseFactory;
import org.springframework.data.mongodb.MongoTransactionManager;
import org.springframework.data.mongodb.config.EnableMongoAuditing;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

/**
 * MongoDB wiring for identity-service — owns its own database {@code exim_identity}.
 *
 * <p>Connection settings come from {@code spring.data.mongodb.*}. The identity services annotate
 * methods with a plain {@code @Transactional}; with a single {@link MongoTransactionManager} bean
 * that resolves unambiguously. The bean is still named {@code identityTransactionManager} to keep
 * the per-service naming convention.
 */
@Configuration
@EnableMongoAuditing
@EnableMongoRepositories(basePackages = "com.eximplatform.identity.repository")
public class IdentityMongoConfig {

    @Bean
    public MongoTransactionManager identityTransactionManager(MongoDatabaseFactory dbFactory) {
        return new MongoTransactionManager(dbFactory);
    }
}
