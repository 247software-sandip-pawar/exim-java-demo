package com.eximplatform.identity.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Provisions a PLATFORM_ADMIN out-of-band, independent of company registration. Guarded by a shared
 * setup secret (app.admin-bootstrap.secret) so it can be called without an existing admin token.
 */
public class BootstrapAdminRequest {

    @NotBlank
    private String name;

    @NotBlank
    @Email
    private String email;

    @NotBlank
    @Size(min = 8, message = "Password must be at least 8 characters")
    private String password;

    @NotBlank
    private String secret;

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public String getSecret() { return secret; }
    public void setSecret(String secret) { this.secret = secret; }
}
