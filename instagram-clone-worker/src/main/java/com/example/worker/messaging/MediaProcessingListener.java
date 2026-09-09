package com.example.worker.messaging;

import com.example.worker.config.RabbitMQConfig;
import com.example.worker.entity.MediaType;
import com.example.worker.entity.PostMedia;
import com.example.worker.entity.PostStatus;
import com.example.worker.repository.PostMediaRepository;
import com.example.worker.service.MediaProcessingService;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Component
public class MediaProcessingListener {

    private final PostMediaRepository postMediaRepository;
    private final MediaProcessingService mediaProcessingService;
    private final RabbitTemplate rabbitTemplate;


    public MediaProcessingListener(PostMediaRepository postMediaRepository, MediaProcessingService mediaProcessingService, RabbitTemplate rabbitTemplate) {
        this.postMediaRepository = postMediaRepository;
        this.mediaProcessingService = mediaProcessingService;
        this.rabbitTemplate = rabbitTemplate;
    }

    @RabbitListener(queues = "media.processing.queue", containerFactory = "rabbitListenerContainerFactory")
    public void handleMessage(MediaProcessingMessage message,
                              @Header(name = "x-death", required = false) List<Map<String, Object>> xDeaths) throws IOException, InterruptedException {
        long retryCount = 0;

        if (xDeaths != null && !xDeaths.isEmpty()) {
            retryCount = (Long) xDeaths.get(0).get("count");
        }

        if (retryCount >= 3) {
            PostMedia postMedia = postMediaRepository.findById(message.getPostMediaId())
                    .orElseThrow(() ->
                            new RuntimeException("PostMedia not found"));

            PostMedia updatedPostMedia = postMedia.toBuilder()
                    .status(PostStatus.FAILED)
                    .build();

            postMediaRepository.save(updatedPostMedia);

            rabbitTemplate.convertAndSend(
                    RabbitMQConfig.DLQ_EXCHANGE,
                    RabbitMQConfig.DLQ_ROUTING_KEY,
                    message
            );

            return;
        }
        PostMedia postMedia = postMediaRepository.findById(message.getPostMediaId())
                .orElseThrow(() -> new RuntimeException("PostMedia not found"));

        String rawKey = message.getRawFileName();

        String extension = rawKey.substring(rawKey.lastIndexOf('.'));
        String fileName = UUID.randomUUID().toString() + extension;

        Path inputPath = Paths.get(
                "temp",
                "raw",
                fileName
        );

        Path outputPath = Paths.get(
                "temp",
                "processed" ,
                fileName
        );

        String thumbnailFileName = fileName.substring(
                0,
                fileName.lastIndexOf('.')
        ) + "_thumb.jpg";

        Path thumbnailPath = Paths.get(
                "temp",
                "processed",
                thumbnailFileName
        );

        mediaProcessingService.downloadFromMinio(
                rawKey,
                inputPath.toString()
        );

        MediaType mediaType = message.getMediaType();

        if (mediaType == MediaType.IMAGE) {
            mediaProcessingService.processImage(
                    inputPath.toString(),
                    outputPath.toString()
            );

            mediaProcessingService.createImageThumbnail(
                    inputPath.toString(),
                    thumbnailPath.toString()
            );

        } else if (mediaType == MediaType.VIDEO) {
            mediaProcessingService.processVideo(
                    inputPath.toString(),
                    outputPath.toString()
            );

            mediaProcessingService.createVideoThumbnail(
                    inputPath.toString(),
                    thumbnailPath.toString()
            );
        }

        String processedKey = "processed/" +fileName;
        String thumbnailKey = "thumbnail/" +thumbnailFileName;

        mediaProcessingService.uploadToMinio(
                outputPath.toString(),
                processedKey
        );

        mediaProcessingService.uploadToMinio(
                thumbnailPath.toString(),
                thumbnailKey
        );

        String mediaUrl = mediaProcessingService.buildMinioUrl(processedKey);
        String thumbnailUrl = mediaProcessingService.buildMinioUrl(thumbnailKey);

        PostMedia updatedPostMedia = postMedia.toBuilder()
                .mediaUrl(mediaUrl)
                .thumbnailUrl(thumbnailUrl)
                .status(PostStatus.READY)
                .build();

        postMediaRepository.save(updatedPostMedia);

        Files.deleteIfExists(inputPath);
        Files.deleteIfExists(outputPath);
        Files.deleteIfExists(thumbnailPath);
    }
}