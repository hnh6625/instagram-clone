package com.example.demo.service;

import com.example.demo.dto.PostResponse;
import com.example.demo.entity.MediaType;
import com.example.demo.entity.Post;
import com.example.demo.entity.PostStatus;
import com.example.demo.entity.User;
import com.example.demo.exception.UnauthorizedActionException;
import com.example.demo.repository.PostRepository;
import com.example.demo.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class PostServiceTest {

    @Mock
    private PostRepository postRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private FileStorageService fileStorageService;
    @Mock
    private RabbitTemplate rabbitTemplate;

    @InjectMocks
    private PostService postService;

    private User user;
    private Post post;
    private List<Post> posts;
    @BeforeEach
    void setUp() {
        user = User.builder()
                .id(10L)
                .username("testuser")
                .build();

        post = Post.builder()
                .id(1L)
                .user(user)
                .caption("Bài viết test")
                .mediaType(MediaType.IMAGE)
                .status(PostStatus.READY)
                .mediaUrl("uploads/processed/abc.jpg")
                .build();

        Post post1 = Post.builder()
                .id(1L)
                .user(user)
                .caption("Bài viết 1")
                .mediaType(MediaType.IMAGE)
                .status(PostStatus.READY)
                .mediaUrl("uploads/processed/abc.jpg")
                .build();

        Post post2 = Post.builder()
                .id(2L)
                .user(user)
                .caption("Bài viết 2")
                .mediaType(MediaType.VIDEO)
                .status(PostStatus.READY)
                .mediaUrl("uploads/processed/video.mp4")
                .build();

        Post post3 = Post.builder()
                .id(3L)
                .user(user)
                .caption("Bài viết 3")
                .mediaType(MediaType.IMAGE)
                .status(PostStatus.READY)
                .mediaUrl("uploads/processed/xyz.jpg")
                .build();
        posts = List.of(post1, post2, post3);
    }
    @Test
    void getPostById_ShouldReturnPost_WhenIdExists() {

        when(postRepository.findById(1L)).thenReturn(Optional.of(post));

        PostResponse result = postService.getPostById(1L);

        assertEquals(1L, result.getId().longValue());
    }

    @Test
    void getFeed_ShouldReturnFeed_WhenFeedExists() {
        when(postRepository.findByStatusOrderByCreatedAtDesc(PostStatus.READY)).thenReturn(posts);
        List<PostResponse> result = postService.getFeed();

        assertEquals(3, result.size());
    }

    @Test
    void deletePost_ShouldDeletePost_WhenUserIsOwner() {
        when(postRepository.findById(1L)).thenReturn(Optional.of(post));
        Long currentUserId = post.getUser().getId();
        postService.deletePost(currentUserId, post.getId());

        Mockito.verify(postRepository).deleteById(post.getId());
    }

    @Test
    void deletePost_ShouldThrowException_WhenUserIsNotOwner() {
        when(postRepository.findById(1L)).thenReturn(Optional.of(post));
        Long currentUserId = post.getUser().getId();


        assertThrows(UnauthorizedActionException.class, () -> {postService.deletePost(2L,post.getId());});
    }
}
