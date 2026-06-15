package com.eximplatform.identity.controller;

import com.eximplatform.common.api.ApiResponse;
import com.eximplatform.common.api.PageResponse;
import com.eximplatform.identity.dto.CreateUserRequest;
import com.eximplatform.identity.dto.UpdateUserRequest;
import com.eximplatform.identity.dto.UserResponse;
import com.eximplatform.identity.service.UserService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/me")
    public ApiResponse<UserResponse> me(Authentication authentication) {
        return ApiResponse.ok(userService.getProfileByEmail(authentication.getName()));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<UserResponse> create(@Valid @RequestBody CreateUserRequest request) {
        return ApiResponse.ok(userService.create(request));
    }

    @GetMapping("/{id}")
    public ApiResponse<UserResponse> get(@PathVariable UUID id) {
        return ApiResponse.ok(userService.get(id));
    }

    @GetMapping
    public ApiResponse<PageResponse<UserResponse>> list(Pageable pageable) {
        return ApiResponse.ok(userService.list(pageable));
    }

    @PutMapping("/{id}")
    public ApiResponse<UserResponse> update(@PathVariable UUID id,
                                            @Valid @RequestBody UpdateUserRequest request) {
        return ApiResponse.ok(userService.update(id, request));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID id) {
        userService.delete(id);
    }
}
