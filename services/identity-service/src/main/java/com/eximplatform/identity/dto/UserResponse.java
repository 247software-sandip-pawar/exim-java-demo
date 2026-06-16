package com.eximplatform.identity.dto;

import com.eximplatform.identity.domain.User;

import java.util.UUID;

public class UserResponse {

    private UUID id;
    private String name;
    private String email;
    private String phone;
    private String role;
    private UUID companyId;
    private String companyName;

    public static UserResponse from(User user) {
        UserResponse r = new UserResponse();
        r.id = user.getId();
        r.name = user.getName();
        r.email = user.getEmail();
        r.phone = user.getPhone();
        r.role = user.getRole().name();
        if (user.getCompany() != null) {
            r.companyId = user.getCompany().getId();
            r.companyName = user.getCompany().getName();
        }
        return r;
    }

    public UUID getId() { return id; }
    public String getName() { return name; }
    public String getEmail() { return email; }
    public String getPhone() { return phone; }
    public String getRole() { return role; }
    public UUID getCompanyId() { return companyId; }
    public String getCompanyName() { return companyName; }
}
