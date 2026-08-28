package com.example.demo.service;

import com.example.demo.dto.UpdateProfileRequest;
import com.example.demo.dto.UserResponse;
import com.example.demo.entity.User;
import com.example.demo.exception.UserNotFoundException;
import com.example.demo.repository.UserRepository;
import org.springframework.stereotype.Service;



@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public UserResponse getCurrentUserProfile(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException("Username not found."));

        return UserResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .bio(user.getBio())
                .avatarUrl(user.getAvatarUrl())
                .createdAt(user.getCreatedAt())
                .build();

    }

    public UserResponse getUserProfileById(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("Id not found."));

        return UserResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .bio(user.getBio())
                .avatarUrl(user.getAvatarUrl())
                .createdAt(user.getCreatedAt())
                .build();
    }

    public UserResponse updateProfile(String username, UpdateProfileRequest request) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException("Username not found."));

        User updatedUser = user.toBuilder()
                .bio(request.getBio())
                .avatarUrl(request.getAvatarUrl())
                .build();

        User result = userRepository.save(updatedUser);
        return UserResponse.builder()
                .id(result.getId())
                .username(result.getUsername())
                .email(result.getEmail())
                .bio(result.getBio())
                .avatarUrl(result.getAvatarUrl())
                .createdAt(result.getCreatedAt())
                .build();
    }
}
