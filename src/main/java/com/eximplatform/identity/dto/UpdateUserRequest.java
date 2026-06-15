package com.eximplatform.identity.dto;

import com.eximplatform.identity.domain.Role;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

/**
 * Update payload for a user. Email and password are managed through dedicated flows and are
 * intentionally not editable here.
 */
public class UpdateUserRequest {

    @NotBlank
    private String name;

    private String phone;

    @NotNull
    private Role role;

    private UUID companyId;

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public Role getRole() { return role; }
    public void setRole(Role role) { this.role = role; }
    public UUID getCompanyId() { return companyId; }
    public void setCompanyId(UUID companyId) { this.companyId = companyId; }
}
