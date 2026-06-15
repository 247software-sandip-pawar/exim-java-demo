package com.eximplatform.identity.dto;

public class AuthResponse {

    private final String accessToken;
    private final String tokenType = "Bearer";
    private final UserResponse user;

    public AuthResponse(String accessToken, UserResponse user) {
        this.accessToken = accessToken;
        this.user = user;
    }

    public String getAccessToken() { return accessToken; }
    public String getTokenType() { return tokenType; }
    public UserResponse getUser() { return user; }
}
