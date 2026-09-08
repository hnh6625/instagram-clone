package com.example.demo.dto;

import com.example.demo.entity.MediaType;
import com.example.demo.entity.PostStatus;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PostMediaResponse {
    private String mediaUrl;
    private String thumbnailUrl;
    private MediaType mediaType;
    private PostStatus status;
    private Integer position;
}