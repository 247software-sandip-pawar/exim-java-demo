package com.eximplatform.identity.service;

import com.eximplatform.common.exception.BusinessException;
import com.eximplatform.identity.domain.Company;
import com.eximplatform.identity.domain.Role;
import com.eximplatform.identity.domain.User;
import com.eximplatform.identity.dto.AuthResponse;
import com.eximplatform.identity.dto.LoginRequest;
import com.eximplatform.identity.dto.RegisterRequest;
import com.eximplatform.identity.dto.UserResponse;
import com.eximplatform.identity.repository.CompanyRepository;
import com.eximplatform.identity.repository.UserRepository;
import com.eximplatform.security.JwtService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final CompanyRepository companyRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    public AuthService(CompanyRepository companyRepository,
                       UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       JwtService jwtService,
                       AuthenticationManager authenticationManager) {
        this.companyRepository = companyRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.authenticationManager = authenticationManager;
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
}
