package com.eximplatform.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.util.ArrayList;
import java.util.List;

/**
 * Shared stateless security for every service: JWT-only, no sessions, method security enabled.
 *
 * <p>Each service authenticates all requests by default. Service-specific public endpoints (e.g.
 * identity's {@code /api/v1/auth/**}) are added via the {@code app.security.public-paths} property;
 * Swagger and the actuator health endpoint are always public.
 */
@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    private static final String[] ALWAYS_PUBLIC = {
            "/v3/api-docs/**",
            "/swagger-ui/**",
            "/swagger-ui.html",
            "/actuator/health"
    };

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final List<String> publicPaths;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter,
                          @Value("${app.security.public-paths:}") List<String> publicPaths) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
        this.publicPaths = publicPaths;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        List<String> permitted = new ArrayList<>(List.of(ALWAYS_PUBLIC));
        permitted.addAll(publicPaths);
        http
            .csrf(AbstractHttpConfigurer::disable)
            .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(permitted.toArray(String[]::new)).permitAll()
                .anyRequest().authenticated())
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }
}
