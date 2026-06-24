package com.eximplatform.identity.service;

import com.eximplatform.common.api.PageResponse;
import com.eximplatform.common.exception.BusinessException;
import com.eximplatform.common.exception.NotFoundException;
import com.eximplatform.identity.domain.Company;
import com.eximplatform.identity.domain.Role;
import com.eximplatform.identity.domain.User;
import com.eximplatform.identity.dto.CreateStaffRequest;
import com.eximplatform.identity.dto.CreateUserRequest;
import com.eximplatform.identity.dto.UpdateUserRequest;
import com.eximplatform.identity.dto.UserResponse;
import com.eximplatform.identity.repository.CompanyRepository;
import com.eximplatform.identity.repository.UserRepository;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;
import java.util.UUID;

@Service
public class UserService {

    private static final Set<Role> STAFF_ROLES = Set.of(Role.PLATFORM_ADMIN, Role.SUPPORT);

    private final UserRepository userRepository;
    private final CompanyRepository companyRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository,
                       CompanyRepository companyRepository,
                       PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.companyRepository = companyRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Loads a user's profile by email and maps it to a DTO <em>inside</em> the transaction, so the
     * lazy {@code company} association is initialized before the persistence context closes
     * (open-in-view is disabled). Entities are never returned to the controller layer.
     */
    @Transactional(readOnly = true)
    public UserResponse getProfileByEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("User not found."));
        return UserResponse.from(user);
    }

    @Transactional
    public UserResponse create(CreateUserRequest req) {
        if (userRepository.existsByEmail(req.getEmail())) {
            throw new BusinessException("EMAIL_ALREADY_REGISTERED",
                    "An account with this email already exists.");
        }
        User user = new User();
        user.setName(req.getName());
        user.setEmail(req.getEmail());
        user.setPasswordHash(passwordEncoder.encode(req.getPassword()));
        user.setPhone(req.getPhone());
        user.setRole(req.getRole());
        user.setCompany(resolveCompany(req.getCompanyId()));
        return UserResponse.from(userRepository.save(user));
    }

    @Transactional(readOnly = true)
    public UserResponse get(UUID id) {
        return UserResponse.from(findOrThrow(id));
    }

    @Transactional(readOnly = true)
    public PageResponse<UserResponse> list(Pageable pageable) {
        return PageResponse.from(userRepository.findAll(pageable), UserResponse::from);
    }

    @Transactional
    public UserResponse update(UUID id, UpdateUserRequest req) {
        User user = findOrThrow(id);
        user.setName(req.getName());
        user.setPhone(req.getPhone());
        user.setRole(req.getRole());
        user.setCompany(resolveCompany(req.getCompanyId()));
        // MongoDB has no dirty-checking; the mutated document must be saved explicitly.
        userRepository.save(user);
        return UserResponse.from(user);
    }

    @Transactional
    public void delete(UUID id) {
        userRepository.delete(findOrThrow(id));
    }

    // --- Platform staff & moderation (PLATFORM_ADMIN) ---

    /** Lists platform staff only (PLATFORM_ADMIN + SUPPORT), not company end users. */
    @Transactional(readOnly = true)
    public PageResponse<UserResponse> listStaff(Pageable pageable) {
        return PageResponse.from(userRepository.findByRoleIn(STAFF_ROLES, pageable), UserResponse::from);
    }

    /** Creates a platform-staff account with no company. Role must be PLATFORM_ADMIN or SUPPORT. */
    @Transactional
    public UserResponse createStaff(CreateStaffRequest req) {
        if (!STAFF_ROLES.contains(req.getRole())) {
            throw new BusinessException("INVALID_STAFF_ROLE",
                    "Staff role must be PLATFORM_ADMIN or SUPPORT.");
        }
        if (userRepository.existsByEmail(req.getEmail())) {
            throw new BusinessException("EMAIL_ALREADY_REGISTERED",
                    "An account with this email already exists.");
        }
        User user = new User();
        user.setName(req.getName());
        user.setEmail(req.getEmail());
        user.setPasswordHash(passwordEncoder.encode(req.getPassword()));
        user.setRole(req.getRole());
        // No company: platform staff operate across all companies.
        return UserResponse.from(userRepository.save(user));
    }

    /** Activates or deactivates any user (moderation). A deactivated user can no longer sign in. */
    @Transactional
    public UserResponse setActive(UUID id, boolean active) {
        User user = findOrThrow(id);
        user.setActive(active);
        userRepository.save(user);
        return UserResponse.from(user);
    }

    private User findOrThrow(UUID id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("User not found."));
    }

    private Company resolveCompany(UUID companyId) {
        if (companyId == null) {
            return null;
        }
        return companyRepository.findById(companyId)
                .orElseThrow(() -> new NotFoundException("Company not found."));
    }
}
