package com.eximplatform.identity.service;

import com.eximplatform.common.exception.BusinessException;
import com.eximplatform.common.exception.NotFoundException;
import com.eximplatform.identity.domain.Company;
import com.eximplatform.identity.domain.Role;
import com.eximplatform.identity.domain.User;
import com.eximplatform.identity.dto.CreateUserRequest;
import com.eximplatform.identity.dto.UpdateUserRequest;
import com.eximplatform.identity.dto.UserResponse;
import com.eximplatform.identity.repository.CompanyRepository;
import com.eximplatform.identity.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock UserRepository userRepository;
    @Mock CompanyRepository companyRepository;
    @Mock PasswordEncoder passwordEncoder;

    @InjectMocks UserService userService;

    private CreateUserRequest createRequest(UUID companyId) {
        CreateUserRequest req = new CreateUserRequest();
        req.setName("Priya Sharma");
        req.setEmail("priya@acme.com");
        req.setPassword("password123");
        req.setPhone("+91-9876543210");
        req.setRole(Role.COMPANY_MEMBER);
        req.setCompanyId(companyId);
        return req;
    }

    @Test
    void create_hashesPasswordAndPersists() {
        when(userRepository.existsByEmail("priya@acme.com")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("HASHED");
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        UserResponse response = userService.create(createRequest(null));

        ArgumentCaptor<User> saved = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(saved.capture());
        assertThat(saved.getValue().getPasswordHash()).isEqualTo("HASHED");
        assertThat(saved.getValue().getEmail()).isEqualTo("priya@acme.com");
        assertThat(response.getRole()).isEqualTo("COMPANY_MEMBER");
    }

    @Test
    void create_rejectsDuplicateEmail() {
        when(userRepository.existsByEmail("priya@acme.com")).thenReturn(true);

        assertThatThrownBy(() -> userService.create(createRequest(null)))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("already exists");

        verify(userRepository, never()).save(any());
    }

    @Test
    void create_failsWhenCompanyMissing() {
        UUID companyId = UUID.randomUUID();
        when(userRepository.existsByEmail("priya@acme.com")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("HASHED");
        when(companyRepository.findById(companyId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.create(createRequest(companyId)))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Company not found");
    }

    @Test
    void create_resolvesCompanyWhenProvided() {
        UUID companyId = UUID.randomUUID();
        Company company = new Company();
        company.setName("Acme Exports");
        when(userRepository.existsByEmail("priya@acme.com")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("HASHED");
        when(companyRepository.findById(companyId)).thenReturn(Optional.of(company));
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        UserResponse response = userService.create(createRequest(companyId));

        assertThat(response.getCompanyName()).isEqualTo("Acme Exports");
    }

    @Test
    void get_returnsProfile() {
        UUID id = UUID.randomUUID();
        User user = new User();
        user.setName("Asha");
        user.setEmail("asha@acme.com");
        user.setRole(Role.COMPANY_ADMIN);
        when(userRepository.findById(id)).thenReturn(Optional.of(user));

        assertThat(userService.get(id).getEmail()).isEqualTo("asha@acme.com");
    }

    @Test
    void get_throwsWhenMissing() {
        UUID id = UUID.randomUUID();
        when(userRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.get(id))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void update_appliesEditableFields() {
        UUID id = UUID.randomUUID();
        User user = new User();
        user.setName("Old Name");
        user.setEmail("asha@acme.com");
        user.setRole(Role.COMPANY_MEMBER);
        when(userRepository.findById(id)).thenReturn(Optional.of(user));

        UpdateUserRequest req = new UpdateUserRequest();
        req.setName("New Name");
        req.setPhone("+91-9000000000");
        req.setRole(Role.COMPANY_ADMIN);

        UserResponse response = userService.update(id, req);

        assertThat(response.getName()).isEqualTo("New Name");
        assertThat(response.getRole()).isEqualTo("COMPANY_ADMIN");
        // email is intentionally not editable here
        assertThat(response.getEmail()).isEqualTo("asha@acme.com");
    }

    @Test
    void delete_removesExistingUser() {
        UUID id = UUID.randomUUID();
        User user = new User();
        when(userRepository.findById(id)).thenReturn(Optional.of(user));

        userService.delete(id);

        verify(userRepository).delete(user);
    }

    @Test
    void getProfileByEmail_throwsWhenMissing() {
        when(userRepository.findByEmail("nobody@acme.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.getProfileByEmail("nobody@acme.com"))
                .isInstanceOf(NotFoundException.class);
    }
}
