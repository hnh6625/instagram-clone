package com.example.demo.controller;

import com.example.demo.dto.PostResponse;
import com.example.demo.entity.Post;
import com.example.demo.security.CustomUserDetails;
import com.example.demo.service.PostService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/posts")
public class PostController {

    private PostService postService;

    public PostController(PostService postService) {
        this.postService = postService;
    }

    @PostMapping(consumes = "multipart/form-data")
    public ResponseEntity<PostResponse> createPost(Authentication authentication,
                                           @RequestParam("file")MultipartFile file,
                                           @RequestParam("caption") String caption,
                                           @RequestParam("mediaType")  String mediaType) throws IOException {
        CustomUserDetails customUserDetails = (CustomUserDetails) authentication.getPrincipal();
        PostResponse result = postService.createPost(customUserDetails.getUsername(), file, caption, mediaType);
        return ResponseEntity.status(HttpStatus.CREATED).body(result);
    }

    @GetMapping("/{id}")
    public ResponseEntity<PostResponse> getPost(@PathVariable Long id){
        PostResponse postResponse = postService.getPostById(id);
        return ResponseEntity.status(HttpStatus.OK).body(postResponse);
    }

    @GetMapping ("/feed")
    public ResponseEntity<List<PostResponse>> getAllPosts(){
        List<PostResponse> postResponse = postService.getFeed();
        return ResponseEntity.status(HttpStatus.OK).body(postResponse);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePost(Authentication authentication,
                                                   @PathVariable Long id){
        CustomUserDetails customUserDetails = (CustomUserDetails) authentication.getPrincipal();
        Long currentId = customUserDetails.getId();
        postService.deletePost(currentId, id);
        return ResponseEntity.noContent().build();
    }
}
