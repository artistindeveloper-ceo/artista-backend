package com.artist_in.app.service;

import com.artist_in.app.dto.filestorage.PresignedUploadResponse;

public interface MediaService {

	PresignedUploadResponse generateUploadUrl(Long userId, String mediaType, String contentType, boolean isVideo);

	String cdnUrl(String key);
}
