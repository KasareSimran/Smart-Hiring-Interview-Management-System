package com.userService.UserService.entity;


import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;

@Entity
public class Role {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    @NotBlank(message = "Role name is required")
    @Column(nullable = false, unique = true)
    private String name;


}
