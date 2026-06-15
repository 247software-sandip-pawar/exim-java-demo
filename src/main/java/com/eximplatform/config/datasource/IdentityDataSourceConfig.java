package com.eximplatform.config.datasource;

import com.eximplatform.identity.domain.Company;
import jakarta.persistence.EntityManagerFactory;
import org.flywaydb.core.Flyway;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.jdbc.autoconfigure.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.annotation.Primary;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;
import java.util.Map;

/**
 * Per-module datasource wiring for the {@code identity} module.
 *
 * <p>Each EXIM module owns its <b>own PostgreSQL database</b> (here {@code exim_identity}). This is
 * the "database per module" pattern inside a single deployable Spring Boot app — the seams that let a
 * module later be extracted into its own service without redesign, while staying simple to run today.
 *
 * <p>This class is the template. To add a new module (e.g. {@code catalog}):
 * <ol>
 *   <li>Create database {@code exim_catalog} (see {@code db/bootstrap/create-databases.sql}).</li>
 *   <li>Add an {@code app.datasource.catalog.*} block to the active profile.</li>
 *   <li>Copy this class to {@code CatalogDataSourceConfig}, point it at the catalog packages and the
 *       {@code db/migration/catalog} migration folder, and <b>remove the {@code @Primary}</b>
 *       annotations (exactly one module — identity — is primary).</li>
 * </ol>
 *
 * <p>The identity module is marked {@code @Primary} so that {@code @Transactional} services that do
 * not name a transaction manager, and Spring Boot's own auto-configuration, resolve to it by default.
 */
@Configuration
@EnableJpaRepositories(
        basePackages = "com.eximplatform.identity.repository",
        entityManagerFactoryRef = "identityEntityManagerFactory",
        transactionManagerRef = "identityTransactionManager"
)
public class IdentityDataSourceConfig {

    /** Binds {@code app.datasource.identity.*} (url / username / password / driver). */
    @Primary
    @Bean
    @ConfigurationProperties("app.datasource.identity")
    public DataSourceProperties identityDataSourceProperties() {
        return new DataSourceProperties();
    }

    /** HikariCP pool for the identity database; pool tuning binds from {@code app.datasource.identity.hikari.*}. */
    @Primary
    @Bean
    @ConfigurationProperties("app.datasource.identity.hikari")
    public DataSource identityDataSource(
            @Qualifier("identityDataSourceProperties") DataSourceProperties properties) {
        return properties.initializeDataSourceBuilder().build();
    }

    /**
     * Runs the identity module's Flyway migrations ({@code db/migration/identity}) against its own
     * database. {@code initMethod = "migrate"} executes the migration as the bean initializes, and the
     * EntityManagerFactory below {@code @DependsOn} this bean so the schema exists before Hibernate
     * validates it.
     */
    @Bean(initMethod = "migrate")
    public Flyway identityFlyway(@Qualifier("identityDataSource") DataSource dataSource) {
        return Flyway.configure()
                .dataSource(dataSource)
                .locations("classpath:db/migration/identity")
                .baselineOnMigrate(true)
                .load();
    }

    /**
     * JPA context scoped to the identity module's entities only.
     *
     * <p>Built via Spring Boot's {@link EntityManagerFactoryBuilder} so it inherits the framework's
     * JPA defaults — notably the camelCase&rarr;snake_case physical naming strategy that maps
     * {@code createdAt} to the {@code created_at} column the Flyway migration creates. Schema is owned
     * by Flyway, so Hibernate only validates entities against it ({@code hbm2ddl.auto=validate}).
     */
    @Primary
    @Bean
    @DependsOn("identityFlyway")
    public LocalContainerEntityManagerFactoryBean identityEntityManagerFactory(
            EntityManagerFactoryBuilder builder,
            @Qualifier("identityDataSource") DataSource dataSource) {
        return builder
                .dataSource(dataSource)
                // Pass a representative entity so refactors that move the package don't silently break wiring.
                .packages(Company.class)
                .persistenceUnit("identity")
                .properties(Map.of("hibernate.hbm2ddl.auto", "validate"))
                .build();
    }

    @Primary
    @Bean
    public PlatformTransactionManager identityTransactionManager(
            @Qualifier("identityEntityManagerFactory") EntityManagerFactory entityManagerFactory) {
        return new JpaTransactionManager(entityManagerFactory);
    }
}
