package com.arthasetu.auth;

import com.arthasetu.security.JwtService;
import com.arthasetu.user.Role;
import com.arthasetu.user.User;
import com.arthasetu.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final org.springframework.security.crypto.password.PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    /**
     * Register a new customer.
     */
    public AuthResponse register(RegisterRequest request) {

        String email = request.getEmail()
                .toLowerCase()
                .trim();

        // Check if email is already registered.
        if (userRepository.existsByEmail(email)) {
            throw new IllegalArgumentException(
                    "Email is already registered");
        }

        // Create new customer.
        User user = User.builder()
                .fullName(request.getFullName().trim())
                .email(email)
                .password(passwordEncoder.encode(request.getPassword()))
                .role(Role.CUSTOMER)
                .enabled(true)
                .build();

        // Save user to database.
        User savedUser = userRepository.save(user);

        return new AuthResponse(
                "Registration successful",
                null,
                savedUser.getId(),
                savedUser.getFullName(),
                savedUser.getEmail(),
                savedUser.getRole().name());
    }

    /**
     * Authenticate an existing user and generate a JWT.
     */
    public AuthResponse login(LoginRequest request) {

        String email = request.getEmail()
                .toLowerCase()
                .trim();

        // Authenticate email + password.
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        email,
                        request.getPassword()));

        // Get the authenticated user from MySQL.
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException(
                        "User not found"));

        // Generate JWT containing email and role.
        String token = jwtService.generateToken(
                user.getEmail(),
                user.getRole().name());

        return new AuthResponse(
                "Login successful",
                token,
                user.getId(),
                user.getFullName(),
                user.getEmail(),
                user.getRole().name());
    }
}