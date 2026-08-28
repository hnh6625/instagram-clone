package com.example.demo.dto;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;

@Data
@Builder
public class UserResponse {
    private Long id;
    private String username;
    private String email;
    private String bio;
    private String avatarUrl;
    private Instant createdAt;
}
