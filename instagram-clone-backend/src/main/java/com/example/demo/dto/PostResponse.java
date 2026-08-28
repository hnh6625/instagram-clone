package com.example.demo.dto;

import com.example.demo.entity.MediaType;
import com.example.demo.entity.PostStatus;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;

@Data
@Builder
public class PostResponse {
    private Long id;
    private UserResponse user;
    private String mediaUrl;
    private MediaType mediaType;
    private String caption;
    private PostStatus status;
    private Instant createdAt;
}
