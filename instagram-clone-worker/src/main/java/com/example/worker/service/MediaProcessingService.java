package com.example.worker.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@Service
public class MediaProcessingService {

    private final S3Client s3Client;

    @Value("${minio.bucket}")
    private String bucketName;

    @Value("${minio.endpoint}")
    private String minioEndpoint;

    public MediaProcessingService(S3Client s3Client) {
        this.s3Client = s3Client;
    }

    public void downloadFromMinio(String key, String localPath) throws IOException {
        Path path =  Paths.get(localPath);
        Files.createDirectories(path.getParent());

        GetObjectRequest request = GetObjectRequest.builder()
                .bucket(bucketName)
                .key(key)
                .build();

        s3Client.getObject(request,path);
    }

    public void uploadToMinio(String localPath,String key) throws IOException {
        Path path = Paths.get(localPath);

        PutObjectRequest request = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(key)
                .build();

        s3Client.putObject(request, RequestBody.fromFile(path));
    }
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

    public String buildMinioUrl(String key) {
        return minioEndpoint + "/" + bucketName + "/" + key;
    }

}
