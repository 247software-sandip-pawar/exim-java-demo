package com.eximplatform.identity.domain;

import com.eximplatform.common.domain.BaseEntity;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "users")
public class User extends BaseEntity {

    private String name;

    @Indexed(unique = true)
    private String email;

    private String passwordHash;

    private String phone;

    private Role role;

    /**
     * Reference to the user's company (stored as a DBRef to the {@code companies} collection, the
     * single source of truth). Resolved eagerly on load, so {@code getCompany().getName()} works in
     * DTO mapping. Querying by the referenced id needs the explicit {@code @Query} in
     * {@code UserRepository.existsByCompanyId}.
     */
    @DBRef
    private Company company;

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public Role getRole() { return role; }
    public void setRole(Role role) { this.role = role; }
    public Company getCompany() { return company; }
    public void setCompany(Company company) { this.company = company; }
}
