package com.example.demo.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;
import java.util.UUID;

@Service
public class FileStorageService {

    private final S3Client s3Client;

    @Value("${minio.bucket}")
    private String bucketName;

    public FileStorageService(S3Client s3Client) {
        this.s3Client = s3Client;
    }

    public String saveRawFile(MultipartFile file) throws IOException {

        String originalFilename = file.getOriginalFilename();

        String extension = "";

        if (originalFilename != null) {
            int dotIndex = originalFilename.lastIndexOf(".");
            if (dotIndex >= 0) {
                extension = originalFilename.substring(dotIndex);
            }
        }

        String fileName = UUID.randomUUID() + extension;
        String rawKey = "raw/" + fileName;
        PutObjectRequest request = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(rawKey)
                .build();

        s3Client.putObject(request, RequestBody.fromInputStream(file.getInputStream(), file.getSize()));
        return rawKey;
    }
}
