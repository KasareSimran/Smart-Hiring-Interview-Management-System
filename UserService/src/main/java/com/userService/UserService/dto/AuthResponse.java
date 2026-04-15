package com.userService.UserService.dto;

import java.util.Set;

public class AuthResponse {
    private String token;
    private String message;
    private String refreshToken;
    private Set<String> roles;


    public AuthResponse() {
    }

    public AuthResponse(String token, String message, Set<String> roles,String refreshToken) {
        this.token = token;
        this.message = message;
        this.roles = roles;
        this.refreshToken=refreshToken;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Set<String> getRoles() {
        return roles;
    }

    public void setRoles(Set<String> roles) {
        this.roles = roles;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }
}
