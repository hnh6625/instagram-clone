package com.example.demo.service;

import com.example.demo.dto.LoginRequest;
import com.example.demo.dto.RegisterRequest;
import com.example.demo.dto.UserResponse;
import com.example.demo.entity.User;
import com.example.demo.exception.InvalidCredentialsException;
import com.example.demo.exception.UserAlreadyExistsException;
import com.example.demo.repository.UserRepository;
import com.example.demo.security.JwtUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class AuthServiceTest {
    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtUtil jwtUtil;

    @InjectMocks
    private AuthService authService;

    @Test
    void register_ShouldThrowException_WhenUsernameAlreadyExists() {
        when(userRepository.findByUsername("ha")).thenReturn(Optional.of(new User()));

        RegisterRequest request = new RegisterRequest();
        request.setUsername("ha");

        assertThrows(UserAlreadyExistsException.class,
                () -> authService.register(request));
    }

    @Test
    void register_ShouldSaveUser_WhenUsernameAndEmailAreAvailable() {
        RegisterRequest request = new RegisterRequest();
        request.setUsername("ha");
        request.setEmail("ha@gmail.com");
        request.setPassword("123123");

        when(userRepository.findByUsername("ha")).thenReturn(Optional.empty());
        when(userRepository.findByEmail("ha@gmail.com")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("123123")).thenReturn("hashPassword123123");

        User savedUser = User.builder()
                .id(1L)
                .username("ha")
                .email("ha@gmail.com")
                .passwordHash("hashPassword123123")
                .build();

        when(userRepository.save(any(User.class)))
                .thenReturn(savedUser);

        UserResponse result = authService.register(request);

        assertEquals("ha", result.getUsername());
        assertEquals(1L,result.getId());
    }

    @Test
    void login_ShouldThrowException_WhenPasswordIsIncorrect() {
        LoginRequest request = new LoginRequest();
        request.setUsername("ha");
        request.setPassword("wrongPassword");

        User existingUser = User.builder()
                .username("ha")
                .passwordHash("correctHashedPassword")
                .build();

        when(userRepository.findByUsername("ha")).thenReturn(Optional.of(existingUser));
        when(passwordEncoder.matches("wrongPassword", "correctHashedPassword"))
                .thenReturn(false);

        assertThrows(InvalidCredentialsException.class, () -> authService.login(request));
    }
}
