package com.eximplatform.config;

import com.eximplatform.identity.domain.Role;
import com.eximplatform.identity.domain.User;
import com.eximplatform.identity.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Seeds a single PLATFORM_ADMIN account on startup if one with the configured email does not yet
 * exist (idempotent). Self-registration only ever creates a COMPANY_ADMIN, so this is how the
 * platform's first administrator account comes into being. The platform admin has no company
 * (UserResponse handles a null company), which is correct — they operate across all companies.
 *
 * Credentials come from config/env so the password is never hard-coded for real deployments:
 *   app.platform-admin.email / .password / .name  (env: PLATFORM_ADMIN_EMAIL / _PASSWORD / _NAME)
 * Set app.platform-admin.enabled=false to skip seeding.
 */
@Configuration
public class PlatformAdminSeeder {

    private static final Logger log = LoggerFactory.getLogger(PlatformAdminSeeder.class);

    @Bean
    CommandLineRunner seedPlatformAdmin(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            @Value("${app.platform-admin.enabled:true}") boolean enabled,
            @Value("${app.platform-admin.email:admin@hirkaniexim.com}") String email,
            @Value("${app.platform-admin.password:Admin@12345}") String password,
            @Value("${app.platform-admin.name:Platform Admin}") String name) {
        return args -> {
            if (!enabled) {
                return;
            }
            if (userRepository.existsByEmail(email)) {
                log.info("Platform admin already present ({}), skipping seed.", email);
                return;
            }
            User admin = new User();
            admin.setName(name);
            admin.setEmail(email);
            admin.setPasswordHash(passwordEncoder.encode(password));
            admin.setRole(Role.PLATFORM_ADMIN);
            // No company: a platform admin operates across all companies.
            userRepository.save(admin);
            log.info("Seeded PLATFORM_ADMIN account: {}", email);
        };
    }
}
