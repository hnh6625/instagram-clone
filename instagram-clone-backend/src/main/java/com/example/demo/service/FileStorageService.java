package com.example.demo.service;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

@Service
public class FileStorageService {

    private final String rawStoragePath = "uploads/raw";

    public String saveRawFile(MultipartFile multipartFile) throws IOException {
        Files.createDirectories(Path.of(rawStoragePath));
        String originalFilename = multipartFile.getOriginalFilename();

        String extension = "";

        if (originalFilename != null) {
            int dotIndex = originalFilename.lastIndexOf(".");
            if (dotIndex >= 0) {
                extension = originalFilename.substring(dotIndex);
            }
        }

        String fileName = UUID.randomUUID() + extension;
        Files.copy(multipartFile.getInputStream(), Path.of(rawStoragePath, fileName));
        return fileName;
    }
}
