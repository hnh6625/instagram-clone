package com.example.demo.dto;

import com.example.demo.entity.MediaType;
import com.example.demo.entity.PostStatus;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.List;

@Data
@Builder
public class PostResponse {
    private Long id;
    private UserResponse user;
    private String caption;
    private PostStatus status;
    private List<PostMediaResponse> media;
    private Instant createdAt;
}
