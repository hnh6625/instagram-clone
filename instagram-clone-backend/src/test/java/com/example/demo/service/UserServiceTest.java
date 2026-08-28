package com.example.demo.service;

import com.example.demo.dto.UpdateProfileRequest;
import com.example.demo.dto.UserResponse;
import com.example.demo.entity.User;
import com.example.demo.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    @Test
    void getCurrentUserProfile_ShouldReturnUser_WhenUsernameExists() {
        User user = User.builder()
                .id(1L)
                .username("ha")
                .email("ha@gmail.com")
                .build();

        when(userRepository.findByUsername("ha")).thenReturn(Optional.of(user));

        UserResponse result = userService.getCurrentUserProfile("ha");

        assertEquals("ha", result.getUsername());
        assertEquals(1L, result.getId());
    }

    @Test
    void getUserProfileById_ShouldReturnUser_WhenIdExists() {
        User user = User.builder()
                .id(2L)
                .username("khuong")
                .email("khuong@gmail.com")
                .build();

        when(userRepository.findById(2L)).thenReturn(Optional.of(user));

        UserResponse result = userService.getUserProfileById(2L);

        assertEquals("khuong", result.getUsername());
        assertEquals(2L, result.getId());
    }

    @Test
    void updateProfile_ShouldUpdateBioAndAvatar_WhenValidRequest() {
        User user = User.builder()
                .id(2L)
                .username("khuong")
                .email("khuong@gmail.com")
                .bio("biocu")
                .avatarUrl("avatarcu")
                .build();

        when(userRepository.findByUsername("khuong")).thenReturn(Optional.of(user));
        UpdateProfileRequest request = UpdateProfileRequest.builder()
                .bio("biomoi")
                .avatarUrl("avatarmoi")
                .build();
        User updatedUser = User.builder()
                .id(2L)
                .username("khuong")
                .email("khuong@gmail.com")
                .bio("biomoi")
                .avatarUrl("avatarmoi")
                .build();

        when(userRepository.save(any(User.class)))
                .thenReturn(updatedUser);


        UserResponse response = userService.updateProfile(user.getUsername(),request);

        assertEquals("biomoi", response.getBio());
        assertEquals("avatarmoi", response.getAvatarUrl());
    }
}