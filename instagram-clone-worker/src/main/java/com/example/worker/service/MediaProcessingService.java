package com.example.worker.service;

import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@Service
public class MediaProcessingService {
    public String processImage(String inputPath, String outputPath) throws IOException, InterruptedException {
        List<String> command = List.of(
                "ffmpeg", "-y",
                "-i", inputPath,
                "-vf", "scale=1080:-1",
                outputPath
        );

        return runFFmpeg(command, outputPath);
    }

    public String processVideo(String inputPath, String outputPath) throws IOException, InterruptedException {
        List<String> command = List.of(
                "ffmpeg", "-y",
                "-i", inputPath,
                "-vcodec", "libx264",
                "-crf", "28", outputPath
        );

        return runFFmpeg(command, outputPath);
    }

    public String createImageThumbnail(String inputPath, String outputPath) throws IOException, InterruptedException {
        List<String> command = List.of(
                "ffmpeg", "-y",
                "-i", inputPath,
                "-vf", "scale=200:200",
                outputPath
        );
        return runFFmpeg(command, outputPath);
    }

    public String createVideoThumbnail(String inputPath, String outputPath)
            throws IOException, InterruptedException {

        List<String> command = List.of(
                "ffmpeg", "-y",
                "-i", inputPath,
                "-ss", "00:00:01",
                "-vframes", "1",
                "-vf", "scale=200:200",
                outputPath
        );

        return runFFmpeg(command, outputPath);
    }

    private String runFFmpeg(List<String> command, String outputPath)
            throws IOException, InterruptedException {

        // Tạo thư mục output nếu chưa tồn tại
        Path output = Paths.get(outputPath);
        Files.createDirectories(output.getParent());

        ProcessBuilder processBuilder = new ProcessBuilder(command);
        processBuilder.redirectErrorStream(true);

        Process process = processBuilder.start();

        process.getInputStream().transferTo(System.out);

        int exitCode = process.waitFor();

        if (exitCode == 0) {
            return outputPath;
        }

        throw new IOException("FFmpeg failed with exit code: " + exitCode);
    }
}
