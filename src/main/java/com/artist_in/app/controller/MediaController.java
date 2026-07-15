package com.artist_in.app.controller;

import java.io.IOException;
import java.io.InputStream;

import com.artist_in.app.service.FileStorageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

/**
 * Serves files that were stored locally by FileStorageServiceImpl. In production
 * you'd typically swap this for direct static file serving (nginx) or a CDN /
 * S3 bucket, but this keeps everything self-contained for local development and
 * easy importing into any environment.
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/media")
@RequiredArgsConstructor

public class MediaController {

    private final FileStorageService fileStorageService;

    @GetMapping("/{category}/{filename}")
    public ResponseEntity<InputStreamResource> getFile(@PathVariable String category, @PathVariable String filename)
            throws IOException {
        String relativePath = category + "/" + filename;
        log.info("Fetching media file: {}", relativePath);

        InputStream inputStream;
        try {
            inputStream = fileStorageService.readFile(relativePath);
        } catch (IOException e) {
            log.error("Failed to read file: {}", relativePath, e);
            throw e;
        }

        MediaType mediaType = resolveMediaType(filename);
        log.debug("Resolved media type '{}' for file: {}", mediaType, filename);

        return ResponseEntity.ok().contentType(mediaType)
                .header(HttpHeaders.CACHE_CONTROL, "public, max-age=31536000, immutable")
                .body(new InputStreamResource(inputStream));
    }

    private MediaType resolveMediaType(String filename) {
        String lower = filename.toLowerCase();
        if (lower.endsWith(".png"))
            return MediaType.IMAGE_PNG;
        if (lower.endsWith(".jpg") || lower.endsWith(".jpeg"))
            return MediaType.IMAGE_JPEG;
        if (lower.endsWith(".gif"))
            return MediaType.IMAGE_GIF;
        if (lower.endsWith(".webp"))
            return MediaType.valueOf("image/webp");
        if (lower.endsWith(".mp4"))
            return MediaType.valueOf("video/mp4");
        if (lower.endsWith(".mov"))
            return MediaType.valueOf("video/quicktime");
        if (lower.endsWith(".webm"))
            return MediaType.valueOf("video/webm");

        log.warn("Unrecognized file extension for '{}', defaulting to application/octet-stream", filename);
        return MediaType.APPLICATION_OCTET_STREAM;
    }
}