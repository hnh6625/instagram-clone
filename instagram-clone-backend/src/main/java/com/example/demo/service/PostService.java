package com.example.demo.service;

import com.example.demo.config.RabbitMQConfig;
import com.example.demo.dto.CreatePostRequest;
import com.example.demo.dto.PostMediaResponse;
import com.example.demo.dto.PostResponse;
import com.example.demo.dto.UserResponse;
import com.example.demo.entity.*;
import com.example.demo.exception.PostNotFoundException;
import com.example.demo.exception.UnauthorizedActionException;
import com.example.demo.exception.UnsupportedMediaTypeException;
import com.example.demo.exception.UserNotFoundException;
import com.example.demo.messaging.MediaProcessingMessage;
import com.example.demo.repository.PostMediaRepository;
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
    private final PostMediaRepository  postMediaRepository;
    private final UserRepository userRepository;
    private final FileStorageService fileStorageService;
    private final RabbitTemplate rabbitTemplate;

    public PostService(PostRepository postRepository, PostMediaRepository postMediaRepository, UserRepository userRepository, FileStorageService fileStorageService, RabbitTemplate rabbitTemplate) {
        this.postRepository = postRepository;
        this.postMediaRepository = postMediaRepository;
        this.userRepository = userRepository;
        this.fileStorageService = fileStorageService;
        this.rabbitTemplate = rabbitTemplate;
    }

    public PostResponse createPost(String username, List<MultipartFile> files, String caption) throws IOException {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException("Không tìm thấy user"));

        Post post = Post.builder()
                .user(user)
                .caption(caption)
                .build();

        Post savedPost  = postRepository.save(post);

        for (int i = 0; i < files.size(); i++) {
            MultipartFile file = files.get(i);
            String rawFilePath = fileStorageService.saveRawFile(file);
            MediaType mediaType = detectMediaType(file.getOriginalFilename());

            PostMedia postMedia = PostMedia.builder()
                    .post(savedPost)
                    .mediaType(mediaType)
                    .status(PostStatus.PROCESSING)
                    .position(i)
                    .build();

            PostMedia savedMedia = postMediaRepository.save(postMedia);

            MediaProcessingMessage message = new MediaProcessingMessage(savedMedia.getId(), rawFilePath, mediaType);
            rabbitTemplate.convertAndSend(RabbitMQConfig.MEDIA_EXCHANGE, RabbitMQConfig.MEDIA_ROUTING_KEY, message);
        }

        return convertToPostResponse(savedPost);
    }

    public PostResponse getPostById(Long id) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new PostNotFoundException("Không tìm thấy post"));

        PostResponse postResponse = convertToPostResponse(post);

        return postResponse;
    }

    public List<PostResponse> getFeed() {
        List<Post> posts = postRepository.OrderByCreatedAtDesc();
        List<PostResponse> postResponseList = new ArrayList<>();

        for (Post post : posts) {
            if (post.getOverallStatus() == PostStatus.READY) {
                postResponseList.add(convertToPostResponse(post));
            }
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

        List<PostMediaResponse> mediaResponses = new ArrayList<>();
        for(PostMedia postMedia : post.getMedia()) {
            mediaResponses.add(PostMediaResponse.builder()
                    .mediaUrl(postMedia.getMediaUrl())
                    .thumbnailUrl(postMedia.getThumbnailUrl())
                    .mediaType(postMedia.getMediaType())
                    .status(postMedia.getStatus())
                    .position(postMedia.getPosition())
                    .build());
        }

        return PostResponse.builder()
                .id(post.getId())
                .user(userResponse)
                .caption(post.getCaption())
                .status(post.getOverallStatus())
                .media(mediaResponses)
                .createdAt(post.getCreatedAt())
                .build();
    }

    private MediaType detectMediaType(String filename) {
        String extension = filename.substring(filename.lastIndexOf(".") + 1).toLowerCase();
        List<String> imageExtensions = List.of("jpg", "jpeg", "png", "webp");
        List<String> videoExtensions = List.of("mp4", "mov", "avi");

        if (imageExtensions.contains(extension)) {
            return MediaType.IMAGE;
        } else if (videoExtensions.contains(extension)) {
            return MediaType.VIDEO;
        } else {
            throw new UnsupportedMediaTypeException("Định dạng file không được hỗ trợ: " + extension);
        }
    }
}



