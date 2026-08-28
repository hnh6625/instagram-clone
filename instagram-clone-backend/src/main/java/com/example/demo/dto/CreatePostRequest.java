package com.example.demo.dto;

import org.springframework.web.multipart.MultipartFile;

public class CreatePostRequest {
    private MultipartFile file;
    private String caption;
    private String mediaType;
}
