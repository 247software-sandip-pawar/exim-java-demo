package com.eximplatform.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Identity-service-only security beans. Only this service issues tokens and authenticates
 * credentials, so the {@link PasswordEncoder} and {@link AuthenticationManager} live here rather
 * than in the shared security library. The stateless filter chain itself is shared
 * ({@code com.eximplatform.security.SecurityConfig}).
 */
@Configuration
public class IdentitySecurityBeans {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}
