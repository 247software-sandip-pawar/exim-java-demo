package com.eximplatform.identity.dto;

import com.eximplatform.identity.domain.CompanyType;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class RegisterRequest {

    @NotBlank
    private String companyName;

    @NotNull
    private CompanyType companyType;

    private String country;

    @NotBlank
    private String adminName;

    @NotBlank
    @Email
    private String email;

    @NotBlank
    @Size(min = 8, message = "Password must be at least 8 characters")
    private String password;

    public String getCompanyName() { return companyName; }
    public void setCompanyName(String companyName) { this.companyName = companyName; }
    public CompanyType getCompanyType() { return companyType; }
    public void setCompanyType(CompanyType companyType) { this.companyType = companyType; }
    public String getCountry() { return country; }
    public void setCountry(String country) { this.country = country; }
    public String getAdminName() { return adminName; }
    public void setAdminName(String adminName) { this.adminName = adminName; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
}
