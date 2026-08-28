package com.example.demo.controller;

import com.example.demo.dto.UpdateProfileRequest;
import com.example.demo.dto.UserResponse;
import com.example.demo.security.CustomUserDetails;
import com.example.demo.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/me")
    public ResponseEntity<UserResponse> getCurrentUser(Authentication authentication) {
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        String username = userDetails.getUsername();
        UserResponse response = userService.getCurrentUserProfile(username);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> getUserProfile(@PathVariable Long id) {
        UserResponse response = userService.getUserProfileById(id);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/me")
    public ResponseEntity<UserResponse> updateProfile(Authentication authentication,
                                                      @RequestBody UpdateProfileRequest request) {
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        String username = userDetails.getUsername();
        UserResponse response = userService.updateProfile(username, request);
        return ResponseEntity.ok().body(response);
    }
}
