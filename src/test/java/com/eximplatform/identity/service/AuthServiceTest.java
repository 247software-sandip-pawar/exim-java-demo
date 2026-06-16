package com.eximplatform.identity.service;

import com.eximplatform.common.exception.BusinessException;
import com.eximplatform.identity.domain.Company;
import com.eximplatform.identity.domain.CompanyType;
import com.eximplatform.identity.domain.Role;
import com.eximplatform.identity.domain.User;
import com.eximplatform.identity.dto.AuthResponse;
import com.eximplatform.identity.dto.LoginRequest;
import com.eximplatform.identity.dto.RegisterRequest;
import com.eximplatform.identity.repository.CompanyRepository;
import com.eximplatform.identity.repository.UserRepository;
import com.eximplatform.security.JwtService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock CompanyRepository companyRepository;
    @Mock UserRepository userRepository;
    @Mock PasswordEncoder passwordEncoder;
    @Mock JwtService jwtService;
    @Mock AuthenticationManager authenticationManager;

    @InjectMocks AuthService authService;

    private RegisterRequest registerRequest() {
        RegisterRequest req = new RegisterRequest();
        req.setCompanyName("Acme Exports");
        req.setCompanyType(CompanyType.EXPORTER);
        req.setCountry("India");
        req.setAdminName("Asha");
        req.setEmail("asha@acme.com");
        req.setPassword("password123");
        return req;
    }

    @Test
    void register_createsCompanyAndAdmin_andReturnsToken() {
        when(userRepository.existsByEmail("asha@acme.com")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("HASHED");
        when(companyRepository.save(any(Company.class))).thenAnswer(inv -> {
            Company c = inv.getArgument(0);
            c.setId(UUID.randomUUID());
            return c;
        });
        when(userRepository.save(any(User.class))).thenAnswer(inv -> {
            User u = inv.getArgument(0);
            u.setId(UUID.randomUUID());
            return u;
        });
        when(jwtService.generateToken(anyString(), anyString(), anyString())).thenReturn("jwt-token");

        AuthResponse response = authService.register(registerRequest());

        assertThat(response.getAccessToken()).isEqualTo("jwt-token");
        assertThat(response.getTokenType()).isEqualTo("Bearer");
        assertThat(response.getUser().getEmail()).isEqualTo("asha@acme.com");

        // First admin of a freshly registered company is a COMPANY_ADMIN with a hashed password.
        ArgumentCaptor<User> saved = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(saved.capture());
        assertThat(saved.getValue().getRole()).isEqualTo(Role.COMPANY_ADMIN);
        assertThat(saved.getValue().getPasswordHash()).isEqualTo("HASHED");
    }

    @Test
    void register_rejectsDuplicateEmail() {
        when(userRepository.existsByEmail("asha@acme.com")).thenReturn(true);

        assertThatThrownBy(() -> authService.register(registerRequest()))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("already exists");

        verify(companyRepository, never()).save(any());
        verify(userRepository, never()).save(any());
    }

    @Test
    void login_authenticatesAndReturnsToken() {
        LoginRequest req = new LoginRequest();
        req.setEmail("asha@acme.com");
        req.setPassword("password123");

        User user = new User();
        user.setId(UUID.randomUUID());
        user.setEmail("asha@acme.com");
        user.setRole(Role.COMPANY_ADMIN);
        when(userRepository.findByEmail("asha@acme.com")).thenReturn(Optional.of(user));
        when(jwtService.generateToken(anyString(), anyString(), anyString())).thenReturn("jwt-token");

        AuthResponse response = authService.login(req);

        assertThat(response.getAccessToken()).isEqualTo("jwt-token");
        verify(authenticationManager).authenticate(any());
    }

    @Test
    void login_propagatesBadCredentials() {
        LoginRequest req = new LoginRequest();
        req.setEmail("asha@acme.com");
        req.setPassword("wrong");
        when(authenticationManager.authenticate(any()))
                .thenThrow(new BadCredentialsException("bad"));

        assertThatThrownBy(() -> authService.login(req))
                .isInstanceOf(BadCredentialsException.class);

        verify(jwtService, never()).generateToken(anyString(), anyString(), anyString());
    }
}
