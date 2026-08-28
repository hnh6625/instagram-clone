package com.example.demo.service;

import com.example.demo.dto.AuthResponse;
import com.example.demo.dto.LoginRequest;
import com.example.demo.dto.RegisterRequest;
import com.example.demo.dto.UserResponse;
import com.example.demo.entity.User;
import com.example.demo.exception.InvalidCredentialsException;
import com.example.demo.exception.UserAlreadyExistsException;
import com.example.demo.repository.UserRepository;
import com.example.demo.security.JwtUtil;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    public UserResponse register(RegisterRequest request) {
        if (userRepository.findByUsername(request.getUsername()).isPresent()) {
            throw new UserAlreadyExistsException("Username already exists");
        }

        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new UserAlreadyExistsException("Email already exists");
        }

        String hashPassword = passwordEncoder.encode(request.getPassword());

        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .passwordHash(hashPassword)
                .build();
        User savedUser = userRepository.save(user);
        return UserResponse.builder()
                .id(savedUser.getId())
                .username(savedUser.getUsername())
                .email(savedUser.getEmail())
                .bio(savedUser.getBio())
                .avatarUrl(savedUser.getAvatarUrl())
                .createdAt(savedUser.getCreatedAt())
                .build();
    }

    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new InvalidCredentialsException("Username not found."));
        String hashedPassword = user.getPasswordHash();
        String rawPassword = request.getPassword();
        if (!(passwordEncoder.matches(rawPassword, hashedPassword))) {
            throw new InvalidCredentialsException("Wrong password");
        }
        String token = jwtUtil.generateToken(request.getUsername());
        return AuthResponse.builder()
                .token(token)
                .username(request.getUsername())
                .build();
    }
}
