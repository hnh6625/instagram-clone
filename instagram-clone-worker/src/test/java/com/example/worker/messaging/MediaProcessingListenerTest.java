package com.example.worker.messaging;

import com.example.worker.entity.MediaType;
import com.example.worker.entity.PostMedia;
import com.example.worker.entity.PostStatus;
import com.example.worker.model.MediaFilePaths;
import com.example.worker.repository.PostMediaRepository;
import com.example.worker.service.MediaProcessingService;
import com.rabbitmq.client.Channel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
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

    @Mock
    private Channel channel;

    @InjectMocks
    private MediaProcessingListener listener;

    private PostMedia postMedia;
    private MediaProcessingMessage message;

    @BeforeEach
    void setUp() {
        message = MediaProcessingMessage.builder()
                .postMediaId(5L)
                .rawFileName("raw/abc.jpg")
                .mediaType(MediaType.VIDEO)
                .build();

        postMedia = PostMedia.builder()
                .id(5L)
                .mediaType(MediaType.VIDEO)
                .status(PostStatus.PROCESSING)
                .position(0)
                .build();

        when(postMediaRepository.findById(5L))
                .thenReturn(Optional.of(postMedia));
    }

    @Test
    void handleMessage_shouldMarkPostMediaReadyWhenProcessingSucceeds()
            throws IOException, InterruptedException {
        doNothing().when(mediaProcessingService)
                        .downloadFromMinio(anyString(),anyString());

        listener.handleMessage(message,null, channel,123L);
        verify(channel).basicAck(123L, false);

        ArgumentCaptor<PostMedia> captor =
                ArgumentCaptor.forClass(PostMedia.class);

        verify(postMediaRepository).save(captor.capture());
        PostMedia savedPostMedia = captor.getValue();
        assertEquals(PostStatus.READY, savedPostMedia.getStatus());
    }

    @Test
    void handleMessage_shouldNotAckWhenProcessingFails() throws IOException, InterruptedException {
        doThrow(new IOException())
                .when(mediaProcessingService)
                .processVideo(anyString(), anyString());

        assertThrows(
                IOException.class,
                () -> listener.handleMessage(message, null, channel, 123L)
        );
        verify(channel, never()).basicAck(anyLong(), anyBoolean());
    }

    @Test
    void handleMessage_shouldMarkFailedAndAckWhenRetryLimitReached() throws IOException, InterruptedException {
        List<Map<String, Object>> xDeaths = List.of(
                Map.of("count", 3L)
        );

        listener.handleMessage(message, xDeaths, channel, 123L);

        ArgumentCaptor<PostMedia> captor =
                ArgumentCaptor.forClass(PostMedia.class);

        verify(postMediaRepository).save(captor.capture());
        PostMedia savedPostMedia = captor.getValue();
        assertEquals(PostStatus.FAILED, savedPostMedia.getStatus());

        verify(channel).basicAck(123L, false);

    }

    @Test
    void handleMessage_shouldNotAckWhenSendingToDlqFails()
            throws IOException, InterruptedException {
        List<Map<String, Object>> xDeaths = List.of(
                Map.of("count", 3L)
        );

        doThrow(new RuntimeException("DLQ send failed"))
                .when(rabbitTemplate)
                .convertAndSend(
                        anyString(),
                        anyString(),
                        any(MediaProcessingMessage.class)
                );

        assertThrows(
                RuntimeException.class,
                () -> listener.handleMessage(message, xDeaths, channel, 123L)
        );

        verify(channel, never()).basicAck(anyLong(), anyBoolean());
    }


}
