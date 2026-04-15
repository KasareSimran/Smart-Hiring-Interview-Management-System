package com.userService.UserService.service;

import com.userService.UserService.dto.AuthResponse;
import com.userService.UserService.dto.LoginRequest;
import com.userService.UserService.dto.RegisterRequest;
import com.userService.UserService.dto.UserResponse;

import java.util.List;

public interface UserService {
    AuthResponse registerUser(RegisterRequest request);

    AuthResponse loginUser(LoginRequest request);

    List<UserResponse> getAllUsers();

    String updateUserStatus(Long userId, String status);
}
