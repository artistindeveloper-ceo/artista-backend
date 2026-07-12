package com.artist_in.app.service;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;

import org.springframework.web.multipart.MultipartFile;

import com.artist_in.app.enums.MediaType;

public interface FileStorageService {

    enum UploadCategory {
        PROFILE_PHOTOS, COVER_PHOTOS, POST_MEDIA, CHAT_ATTACHMENTS
    }

    record StoredMedia(String url, String thumbnailUrl, MediaType mediaType) {
    }

    record VideoStoreResult(String videoUrl, String thumbnailUrl) {
    }

    String storeImage(MultipartFile file, UploadCategory category);

    VideoStoreResult storeVideo(MultipartFile file, UploadCategory category);

    StoredMedia storeMedia(MultipartFile file, UploadCategory category);

    Path resolvePath(String relativePath);

    InputStream readFile(String relativePath) throws IOException;

}