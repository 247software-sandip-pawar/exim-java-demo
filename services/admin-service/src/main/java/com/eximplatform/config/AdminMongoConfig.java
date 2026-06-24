package com.eximplatform.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.MongoDatabaseFactory;
import org.springframework.data.mongodb.MongoTransactionManager;
import org.springframework.data.mongodb.config.EnableMongoAuditing;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

/**
 * MongoDB wiring for admin-service — owns its own database {@code exim_admin}.
 * Connection settings come from {@code spring.data.mongodb.*}; this enables repositories, auditing,
 * and a transaction manager named to match {@code @Transactional(transactionManager =
 * "adminTransactionManager")}.
 */
@Configuration
@EnableMongoAuditing
@EnableMongoRepositories(basePackages = "com.eximplatform.admin.repository")
public class AdminMongoConfig {

    @Bean
    public MongoTransactionManager adminTransactionManager(MongoDatabaseFactory dbFactory) {
        return new MongoTransactionManager(dbFactory);
    }
}
