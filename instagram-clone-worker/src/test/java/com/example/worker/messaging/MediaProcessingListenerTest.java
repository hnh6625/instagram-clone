package com.example.worker.messaging;

import com.example.worker.entity.MediaType;
import com.example.worker.entity.PostMedia;
import com.example.worker.entity.PostStatus;
import com.example.worker.repository.PostMediaRepository;
import com.example.worker.service.MediaProcessingService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import java.io.IOException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class MediaProcessingListenerTest {
    @Mock
    private PostMediaRepository postMediaRepository;

    @Mock
    private MediaProcessingService mediaProcessingService;

    @Mock
    private RabbitTemplate rabbitTemplate;

    @InjectMocks
    private MediaProcessingListener listener;

    @Test
    void handleMessage_shouldMarkPostMediaReadyWhenProcessingSucceeds()
            throws IOException, InterruptedException {
        MediaProcessingMessage message = MediaProcessingMessage.builder()
                .postMediaId(5L)
                .rawFileName("raw/abc.jpg")
                .mediaType(MediaType.IMAGE)
                .build();

        PostMedia postMedia = PostMedia.builder()
                .id(5L)
                .mediaType(MediaType.IMAGE)
                .status(PostStatus.PROCESSING)
                .position(0)
                .build();

        when(postMediaRepository.findById(5L))
                .thenReturn(Optional.of(postMedia));

        doNothing().when(mediaProcessingService)
                        .downloadFromMinio(anyString(),anyString());
        listener.handleMessage(message,null);

        ArgumentCaptor<PostMedia> captor =
                ArgumentCaptor.forClass(PostMedia.class);

        verify(postMediaRepository).save(captor.capture());
        PostMedia savedPostMedia = captor.getValue();
        assertEquals(PostStatus.READY, savedPostMedia.getStatus());
    }
}
