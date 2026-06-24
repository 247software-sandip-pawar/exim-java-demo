package com.eximplatform.identity.controller;

import com.eximplatform.common.api.ApiResponse;
import com.eximplatform.identity.dto.AuthResponse;
import com.eximplatform.identity.dto.BootstrapAdminRequest;
import com.eximplatform.identity.dto.LoginRequest;
import com.eximplatform.identity.dto.RegisterRequest;
import com.eximplatform.identity.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ApiResponse<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        return ApiResponse.ok(authService.register(request));
    }

    @PostMapping("/login")
    public ApiResponse<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        return ApiResponse.ok(authService.login(request));
    }

    /** Platform-staff-only sign-in for the admin portal (rejects company accounts). */
    @PostMapping("/admin-login")
    public ApiResponse<AuthResponse> adminLogin(@Valid @RequestBody LoginRequest request) {
        return ApiResponse.ok(authService.adminLogin(request));
    }

    /** Provision a PLATFORM_ADMIN out-of-band, guarded by the shared setup secret. */
    @PostMapping("/bootstrap-admin")
    public ApiResponse<AuthResponse> bootstrapAdmin(@Valid @RequestBody BootstrapAdminRequest request) {
        return ApiResponse.ok(authService.bootstrapAdmin(request));
    }
}
