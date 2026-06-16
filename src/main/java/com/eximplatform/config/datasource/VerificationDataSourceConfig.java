package com.eximplatform.config.datasource;

import com.eximplatform.verification.domain.Verification;
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
 * Per-module datasource wiring for the {@code verification} module — its own PostgreSQL database
 * ({@code exim_verification}). Built from the {@link IdentityDataSourceConfig} template; note there
 * is <b>no {@code @Primary}</b> here (exactly one module — identity — is primary). Services in this
 * module must therefore name {@code verificationTransactionManager} on {@code @Transactional}.
 */
@Configuration
@EnableJpaRepositories(
        basePackages = "com.eximplatform.verification.repository",
        entityManagerFactoryRef = "verificationEntityManagerFactory",
        transactionManagerRef = "verificationTransactionManager"
)
public class VerificationDataSourceConfig {

    @Bean
    @ConfigurationProperties("app.datasource.verification")
    public DataSourceProperties verificationDataSourceProperties() {
        return new DataSourceProperties();
    }

    @Bean
    @ConfigurationProperties("app.datasource.verification.hikari")
    public DataSource verificationDataSource(
            @Qualifier("verificationDataSourceProperties") DataSourceProperties properties) {
        return properties.initializeDataSourceBuilder().build();
    }

    @Bean(initMethod = "migrate")
    public Flyway verificationFlyway(@Qualifier("verificationDataSource") DataSource dataSource) {
        return Flyway.configure()
                .dataSource(dataSource)
                .locations("classpath:db/migration/verification")
                .baselineOnMigrate(true)
                .load();
    }

    @Bean
    @DependsOn("verificationFlyway")
    public LocalContainerEntityManagerFactoryBean verificationEntityManagerFactory(
            EntityManagerFactoryBuilder builder,
            @Qualifier("verificationDataSource") DataSource dataSource) {
        return builder
                .dataSource(dataSource)
                .packages(Verification.class)
                .persistenceUnit("verification")
                .properties(Map.of("hibernate.hbm2ddl.auto", "validate"))
                .build();
    }

    @Bean
    public PlatformTransactionManager verificationTransactionManager(
            @Qualifier("verificationEntityManagerFactory") EntityManagerFactory entityManagerFactory) {
        return new JpaTransactionManager(entityManagerFactory);
    }
}
