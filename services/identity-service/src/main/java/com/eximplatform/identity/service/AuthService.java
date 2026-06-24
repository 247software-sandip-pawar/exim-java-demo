package com.eximplatform.identity.service;

import com.eximplatform.common.exception.ApiException;
import com.eximplatform.common.exception.BusinessException;
import com.eximplatform.identity.domain.Company;
import com.eximplatform.identity.domain.Role;
import com.eximplatform.identity.domain.User;
import com.eximplatform.identity.dto.AuthResponse;
import com.eximplatform.identity.dto.BootstrapAdminRequest;
import com.eximplatform.identity.dto.LoginRequest;
import com.eximplatform.identity.dto.RegisterRequest;
import com.eximplatform.identity.dto.UserResponse;
import com.eximplatform.identity.repository.CompanyRepository;
import com.eximplatform.identity.repository.UserRepository;
import com.eximplatform.security.JwtService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

@Service
public class AuthService {

    /** Roles allowed to use the dedicated admin entry point. */
    private static final Set<Role> STAFF_ROLES = Set.of(Role.PLATFORM_ADMIN, Role.SUPPORT);

    private final CompanyRepository companyRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final String bootstrapSecret;

    public AuthService(CompanyRepository companyRepository,
                       UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       JwtService jwtService,
                       AuthenticationManager authenticationManager,
                       @Value("${app.admin-bootstrap.secret:}") String bootstrapSecret) {
        this.companyRepository = companyRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.authenticationManager = authenticationManager;
        this.bootstrapSecret = bootstrapSecret;
    }

    /** Registers a company and its first admin user, returning a token. */
    @Transactional
    public AuthResponse register(RegisterRequest req) {
        if (userRepository.existsByEmail(req.getEmail())) {
            throw new BusinessException("EMAIL_ALREADY_REGISTERED",
                    "An account with this email already exists.");
        }

        Company company = new Company();
        company.setName(req.getCompanyName());
        company.setType(req.getCompanyType());
        company.setCountry(req.getCountry());
        company = companyRepository.save(company);

        User user = new User();
        user.setName(req.getAdminName());
        user.setEmail(req.getEmail());
        user.setPasswordHash(passwordEncoder.encode(req.getPassword()));
        user.setRole(Role.COMPANY_ADMIN);
        user.setCompany(company);
        user = userRepository.save(user);

        String token = jwtService.generateToken(
                user.getEmail(), user.getId().toString(), user.getRole().name());
        return new AuthResponse(token, UserResponse.from(user));
    }

    /** Authenticates credentials and returns a token. */
    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest req) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(req.getEmail(), req.getPassword()));
        User user = userRepository.findByEmail(req.getEmail()).orElseThrow();
        String token = jwtService.generateToken(
                user.getEmail(), user.getId().toString(), user.getRole().name());
        return new AuthResponse(token, UserResponse.from(user));
    }

    /**
     * Dedicated entry point for platform staff (the admin portal). Authenticates exactly like
     * {@link #login} but rejects anyone who isn't a PLATFORM_ADMIN or SUPPORT, so end-user
     * (company) accounts can't sign in here.
     */
    @Transactional(readOnly = true)
    public AuthResponse adminLogin(LoginRequest req) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(req.getEmail(), req.getPassword()));
        User user = userRepository.findByEmail(req.getEmail()).orElseThrow();
        if (!STAFF_ROLES.contains(user.getRole())) {
            throw new ApiException(HttpStatus.FORBIDDEN, "NOT_PLATFORM_STAFF",
                    "This portal is for platform staff only.");
        }
        String token = jwtService.generateToken(
                user.getEmail(), user.getId().toString(), user.getRole().name());
        return new AuthResponse(token, UserResponse.from(user));
    }

    /**
     * Provisions a PLATFORM_ADMIN out-of-band, guarded by the shared setup secret. Independent of
     * company registration: the created admin has no company. Disabled if no secret is configured.
     */
    @Transactional
    public AuthResponse bootstrapAdmin(BootstrapAdminRequest req) {
        if (bootstrapSecret == null || bootstrapSecret.isBlank()
                || !bootstrapSecret.equals(req.getSecret())) {
            throw new ApiException(HttpStatus.FORBIDDEN, "BOOTSTRAP_FORBIDDEN",
                    "Invalid or missing setup secret.");
        }
        if (userRepository.existsByEmail(req.getEmail())) {
            throw new BusinessException("EMAIL_ALREADY_REGISTERED",
                    "An account with this email already exists.");
        }
        User admin = new User();
        admin.setName(req.getName());
        admin.setEmail(req.getEmail());
        admin.setPasswordHash(passwordEncoder.encode(req.getPassword()));
        admin.setRole(Role.PLATFORM_ADMIN);
        // No company: a platform admin operates across all companies.
        admin = userRepository.save(admin);
        String token = jwtService.generateToken(
                admin.getEmail(), admin.getId().toString(), admin.getRole().name());
        return new AuthResponse(token, UserResponse.from(admin));
    }
}
