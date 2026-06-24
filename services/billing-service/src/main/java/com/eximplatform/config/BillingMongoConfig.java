package com.eximplatform.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.MongoDatabaseFactory;
import org.springframework.data.mongodb.MongoTransactionManager;
import org.springframework.data.mongodb.config.EnableMongoAuditing;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

/**
 * MongoDB wiring for billing-service — owns its own database {@code exim_billing}.
 * Connection settings come from {@code spring.data.mongodb.*}; this enables repositories, auditing,
 * and a transaction manager named to match {@code @Transactional(transactionManager =
 * "billingTransactionManager")}.
 */
@Configuration
@EnableMongoAuditing
@EnableMongoRepositories(basePackages = "com.eximplatform.billing.repository")
public class BillingMongoConfig {

    @Bean
    public MongoTransactionManager billingTransactionManager(MongoDatabaseFactory dbFactory) {
        return new MongoTransactionManager(dbFactory);
    }
}
