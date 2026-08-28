package com.example.worker.messaging;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MediaProcessingMessage {
    private Long postId;
    private String rawFileName;
    private String mediaType;
}