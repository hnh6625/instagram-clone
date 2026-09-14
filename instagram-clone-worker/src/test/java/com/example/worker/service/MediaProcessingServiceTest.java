package com.example.worker.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.services.s3.S3Client;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(MockitoExtension.class)
public class MediaProcessingServiceTest {
    @Mock
    private S3Client s3Client;

    private MediaProcessingService service;

    @BeforeEach
    void setUp() throws IOException, InterruptedException {
        service = new MediaProcessingService(s3Client);
        inputPath = tempDir.resolve("test-video.mp4");
        outputDirectory = tempDir.resolve("hls");
        Files.createDirectories(outputDirectory);

        List<String> command = List.of(
                "ffmpeg", "-y",
                "-f","lavfi", //FFmpeg dùng bộ lọc tạo dữ liệu giả.
                "-i","color=c=black:s=320x420:d=2", // tạo video nền đen - d2: dài 2s
                "-c:v","libx264", //encode H.264
                inputPath.toString()
        );

        Process process = new ProcessBuilder(command)
                .redirectErrorStream(true)
                .start();

        process.getInputStream().transferTo(System.out);
        int exitCode = process.waitFor();
        assertEquals(0,exitCode);

    }
    @TempDir
    Path tempDir;

    private Path inputPath;
    private Path outputDirectory;
    @Test
    void createHls_shouldCreatePlaylistWhenProcessingSucceeds() throws Exception {
        service.createHls(
                inputPath.toString(),
                outputDirectory.toString()
        );

        Path playlistPath = outputDirectory.resolve("playlist.m3u8");

        assertTrue(Files.exists(playlistPath));
    }

    @Test
    void createHls_shouldCreateSegmentsWhenProcessingSucceeds() throws Exception {
        service.createHls(
                inputPath.toString(),
                outputDirectory.toString()
        );

        assertTrue(Files.list(outputDirectory).anyMatch(path -> path.toString().endsWith(".ts")));
    }
}
