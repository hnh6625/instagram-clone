package com.example.demo.messaging;

import com.example.demo.entity.MediaType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MediaProcessingMessage {
    private Long postMediaId;
    private String rawFileName;
    private MediaType mediaType;
}
