package com.example.demo.service;

import com.example.demo.config.RabbitMQConfig;
import com.example.demo.dto.CreatePostRequest;
import com.example.demo.dto.PostResponse;
import com.example.demo.dto.UserResponse;
import com.example.demo.entity.MediaType;
import com.example.demo.entity.Post;
import com.example.demo.entity.PostStatus;
import com.example.demo.entity.User;
import com.example.demo.exception.PostNotFoundException;
import com.example.demo.exception.UnauthorizedActionException;
import com.example.demo.exception.UserNotFoundException;
import com.example.demo.messaging.MediaProcessingMessage;
import com.example.demo.repository.PostRepository;
import com.example.demo.repository.UserRepository;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class PostService {

    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final FileStorageService fileStorageService;
    private final RabbitTemplate rabbitTemplate;

    public PostService(PostRepository postRepository, UserRepository userRepository, FileStorageService fileStorageService, RabbitTemplate rabbitTemplate) {
        this.postRepository = postRepository;
        this.userRepository = userRepository;
        this.fileStorageService = fileStorageService;
        this.rabbitTemplate = rabbitTemplate;
    }

    public PostResponse createPost(String username, MultipartFile file, String caption, String mediaType) throws IOException {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException("Không tìm thấy user"));

        String rawFilePath = fileStorageService.saveRawFile(file);

        Post post = Post.builder()
                .user(user)
                .mediaType(MediaType.valueOf(mediaType))
                .status(PostStatus.PROCESSING)
                .caption(caption)
                .mediaUrl(null)
                .build();

        Post result = postRepository.save(post);

        MediaProcessingMessage message = new MediaProcessingMessage(result.getId(), rawFilePath,mediaType);

        rabbitTemplate.convertAndSend(RabbitMQConfig.MEDIA_EXCHANGE, RabbitMQConfig.MEDIA_ROUTING_KEY, message);
        return convertToPostResponse(result);
    }

    public PostResponse getPostById(Long id) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new PostNotFoundException("Không tìm thấy post"));

        PostResponse postResponse = convertToPostResponse(post);

        return postResponse;
    }

    public List<PostResponse> getFeed() {
        List<Post> posts = postRepository.findByStatusOrderByCreatedAtDesc(PostStatus.READY);
        List<PostResponse> postResponseList = new ArrayList<>();

        for (Post post : posts) {
            PostResponse postResponse = convertToPostResponse(post);
            postResponseList.add(postResponse);
        }
        return postResponseList;
    }

    public void deletePost(Long currentUserId, Long postId) {
        Post postFound= postRepository.findById(postId)
                .orElseThrow(() -> new PostNotFoundException("Post not found"));
        if (currentUserId.equals(postFound.getUser().getId())) {
            postRepository.deleteById(postId);
        } else {
            throw new UnauthorizedActionException("Không có quyền xóa post này.");
        }
    }

    private PostResponse convertToPostResponse(Post post) {
        User user = post.getUser();

        UserResponse userResponse = UserResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .avatarUrl(user.getAvatarUrl())
                .build();

        return PostResponse.builder()
                .id(post.getId())
                .user(userResponse)
                .mediaUrl(post.getMediaUrl())
                .mediaType(post.getMediaType())
                .thumbnailUrl(post.getThumbnailUrl())
                .caption(post.getCaption())
                .status(post.getStatus())
                .createdAt(post.getCreatedAt())
                .build();
    }
}
