package com.eximplatform.config.datasource;

import com.eximplatform.sourcing.domain.Rfq;
import jakarta.persistence.EntityManagerFactory;
import org.flywaydb.core.Flyway;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.jdbc.autoconfigure.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;
import java.util.Map;

/**
 * Datasource wiring for sourcing-service — owns its own database {@code exim_sourcing}.
 */
@Configuration
@EnableJpaRepositories(
        basePackages = "com.eximplatform.sourcing.repository",
        entityManagerFactoryRef = "sourcingEntityManagerFactory",
        transactionManagerRef = "sourcingTransactionManager"
)
public class SourcingDataSourceConfig {

    @Bean
    @ConfigurationProperties("app.datasource.sourcing")
    public DataSourceProperties sourcingDataSourceProperties() {
        return new DataSourceProperties();
    }

    @Bean
    @ConfigurationProperties("app.datasource.sourcing.hikari")
    public DataSource sourcingDataSource(
            @Qualifier("sourcingDataSourceProperties") DataSourceProperties properties) {
        return properties.initializeDataSourceBuilder().build();
    }

    @Bean(initMethod = "migrate")
    public Flyway sourcingFlyway(@Qualifier("sourcingDataSource") DataSource dataSource) {
        return Flyway.configure()
                .dataSource(dataSource)
                .locations("classpath:db/migration/sourcing")
                .baselineOnMigrate(true)
                .load();
    }

    @Bean
    @DependsOn("sourcingFlyway")
    public LocalContainerEntityManagerFactoryBean sourcingEntityManagerFactory(
            EntityManagerFactoryBuilder builder,
            @Qualifier("sourcingDataSource") DataSource dataSource) {
        return builder
                .dataSource(dataSource)
                .packages(Rfq.class)
                .persistenceUnit("sourcing")
                .properties(Map.of("hibernate.hbm2ddl.auto", "validate"))
                .build();
    }

    @Bean
    public PlatformTransactionManager sourcingTransactionManager(
            @Qualifier("sourcingEntityManagerFactory") EntityManagerFactory entityManagerFactory) {
        return new JpaTransactionManager(entityManagerFactory);
    }
}
