package com.userService.UserService.service;


import com.userService.UserService.config.JwtProvider;
import com.userService.UserService.dto.AuthResponse;
import com.userService.UserService.dto.LoginRequest;
import com.userService.UserService.dto.RegisterRequest;
import com.userService.UserService.dto.UserResponse;
import com.userService.UserService.entity.Role;
import com.userService.UserService.entity.Status;
import com.userService.UserService.entity.User;
import com.userService.UserService.exception.CustomException;
import com.userService.UserService.repository.RoleRepository;
import com.userService.UserService.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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


    private static final Logger logger =
            LoggerFactory.getLogger(UserServiceImpl.class);

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

        logger.info("Register request received for email: {}", request.getEmail());

        // Check if user exists

        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            logger.error("Registration failed - Email already exists: {}", request.getEmail());
            throw new CustomException("Email already registered");
        }

        // Assign role
        String roleName = request.getRole();
        if (roleName == null || roleName.isEmpty()) {
            roleName = "ROLE_USER";
            logger.info("No role provided. Assigning default role: {}", roleName);
        }

        Role role = roleRepository.findByName(roleName)
                .orElseThrow(() -> new CustomException("Role not found"));

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
        logger.info("User registered successfully: {}", user.getEmail());




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

        logger.info("Login attempt for email: {}", request.getEmail());

        // Authenticate
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getEmail(),
                            request.getPassword()
                    )
            );
        } catch (Exception e) {
            logger.error("Login failed for email: {}", request.getEmail());
            throw new CustomException("Invalid credentials");
        }



        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> {
                    logger.error("User not found during login: {}", request.getEmail());
                    return new CustomException("User not found");
                });

        if (user.getStatus() != Status.ACTIVE) {
            logger.error("Login blocked - User inactive: {}", request.getEmail());
            throw new CustomException("User is inactive");
        }



        logger.info("Login successful for email: {}", request.getEmail());

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

        logger.info("Fetching all users");

        List<User> users = userRepository.findAll();

        logger.info("Total users fetched: {}", users.size());

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

        logger.info("Updating status for userId: {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> {
                    logger.error("User not found with id: {}", userId);
                    return new CustomException("User not found");
                });

        try {
            Status newStatus = Status.valueOf(status.toUpperCase());
            user.setStatus(newStatus);
        } catch (Exception e) {
            logger.error("Invalid status provided: {}", status);
            throw new CustomException("Invalid status");
        }


        userRepository.save(user);

        logger.info("User status updated successfully for userId: {}", userId);

        return "User status updated successfully";
    }
}
