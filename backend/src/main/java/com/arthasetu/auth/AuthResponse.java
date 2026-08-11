package com.arthasetu.auth;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class AuthResponse {

    private String message;
    private String token;
    private Long userId;
    private String fullName;
    private String email;
    private String role;
}