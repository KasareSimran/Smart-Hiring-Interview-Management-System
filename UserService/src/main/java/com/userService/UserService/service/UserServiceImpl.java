package com.userService.UserService.service;


import com.userService.UserService.config.JwtProvider;
import com.userService.UserService.dto.AuthResponse;
import com.userService.UserService.dto.LoginRequest;
import com.userService.UserService.dto.RegisterRequest;
import com.userService.UserService.dto.UserResponse;
import com.userService.UserService.entity.Role;
import com.userService.UserService.entity.Status;
import com.userService.UserService.entity.User;
import com.userService.UserService.repository.RoleRepository;
import com.userService.UserService.repository.UserRepository;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class UserServiceImpl implements UserService{

    private UserRepository userRepository;
    private RoleRepository roleRepository;
    private JwtProvider jwtProvider;
    private AuthenticationManager authenticationManager;
    private PasswordEncoder passwordEncoder;

    public UserServiceImpl(UserRepository userRepository,
                           RoleRepository roleRepository,
                           JwtProvider jwtProvider,
                           AuthenticationManager authenticationManager,
                           PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.jwtProvider = jwtProvider;
        this.authenticationManager = authenticationManager;
        this.passwordEncoder = passwordEncoder;
    }



    //register user
    @Override
    public AuthResponse registerUser(RegisterRequest request) {
        // Check if user exists
        userRepository.findByEmail(request.getEmail())
                .ifPresent(user -> {
                    throw new RuntimeException("Email already registered");
                });

        // Assign role
        String roleName = request.getRole() != null
                ? request.getRole()
                : "ROLE_USER";

        Role role = roleRepository.findByName(roleName)
                .orElseThrow(() -> new RuntimeException("Role not found"));

        Set<Role> roles = new HashSet<>();
        roles.add(role);

        // Create User
        User user = new User();
        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRoles(roles);
        user.setStatus(Status.ACTIVE);

        userRepository.save(user);



        // Generate token
        String token = jwtProvider.generateAccessToken(user.getEmail());

        AuthResponse response = new AuthResponse();
        response.setToken(token);
        response.setMessage("User registered successfully");
        // convert role to Set
        Set<String> rolesSet = new HashSet<>();
        rolesSet.add(roleName);

        response.setRoles(rolesSet);



        return response;
    }



    //login user
    @Override
    public AuthResponse loginUser(LoginRequest request) {

        // Authenticate
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));

        String token = jwtProvider.generateAccessToken(user.getEmail());

        AuthResponse response = new AuthResponse();
        response.setToken(token);
        response.setMessage("Login successful");
        Set<String> roles = user.getRoles().stream()
                .map(Role::getName)
                .collect(Collectors.toSet());

        response.setRoles(roles);

        return response;
    }


    //get all users
    @Override
    public List<UserResponse> getAllUsers() {

        List<User> users = userRepository.findAll();

        List<UserResponse> responseList = new ArrayList<>();

        for (User user : users) {

            UserResponse response = new UserResponse();

            response.setId(user.getId());
            response.setName(user.getName());
            response.setEmail(user.getEmail());

            // Convert roles
            Set<String> roles = user.getRoles().stream()
                    .map(role -> role.getName())
                    .collect(Collectors.toSet());

            response.setRoles(roles);

            response.setStatus(user.getStatus().name());
            response.setCreatedAt(user.getCreatedAt());

            responseList.add(response);
        }

        return responseList;
    }



    //update user
    @Override
    public String updateUserStatus(Long userId, String status) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        try {
            Status newStatus = Status.valueOf(status.toUpperCase());
            user.setStatus(newStatus);
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Invalid status value");
        }

        userRepository.save(user);

        return "User status updated successfully";
    }
}
