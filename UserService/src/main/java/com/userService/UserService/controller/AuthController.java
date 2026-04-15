package com.userService.UserService.controller;


import com.userService.UserService.dto.AuthResponse;
import com.userService.UserService.dto.LoginRequest;
import com.userService.UserService.dto.RegisterRequest;
import com.userService.UserService.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {
    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    // REGISTER
    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@RequestBody RegisterRequest request) {
        return ResponseEntity.ok(userService.registerUser(request));
    }

    // LOGIN
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest request) {
        return ResponseEntity.ok(userService.loginUser(request));
    }

    // REFRESH TOKEN
    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refresh(
            @CookieValue("refreshToken") String token) {
        return ResponseEntity.ok(userService.refreshToken(token));
    }

    //LOGOUT
    @PostMapping("/logout")
    public ResponseEntity<String> logout(@RequestParam String token) {
        userService.logout(token);
        return ResponseEntity.ok("Logged out successfully");
    }

}
