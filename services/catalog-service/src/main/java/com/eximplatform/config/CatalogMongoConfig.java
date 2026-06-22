package com.eximplatform.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.MongoDatabaseFactory;
import org.springframework.data.mongodb.MongoTransactionManager;
import org.springframework.data.mongodb.config.EnableMongoAuditing;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

/**
 * MongoDB wiring for catalog-service — owns its own database {@code exim_catalog}.
 *
 * <p>The connection (URI + database name) comes from {@code spring.data.mongodb.*} in
 * {@code application.yml}; this class only enables the repositories, auditing (created/updated
 * timestamps on {@code BaseEntity}), and a transaction manager named to match the service's
 * {@code @Transactional(transactionManager = "catalogTransactionManager")}.
 *
 * <p>MongoDB multi-document transactions require a replica set — MongoDB Atlas is one. Against a
 * standalone {@code mongod} the transaction would fail; use the Atlas URI or a single-node replica
 * set for local development.
 */
@Configuration
@EnableMongoAuditing
@EnableMongoRepositories(basePackages = "com.eximplatform.catalog.repository")
public class CatalogMongoConfig {

    @Bean
    public MongoTransactionManager catalogTransactionManager(MongoDatabaseFactory dbFactory) {
        return new MongoTransactionManager(dbFactory);
    }
}
