package com.example.worker.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.nio.file.Path;


@Getter
@RequiredArgsConstructor
public class MediaFilePaths {

    private final Path inputPath;
    private final Path outputPath;
    private final Path thumbnailPath;

}