package com.artist_in.app.dto.filestorage;

public record PresignedUploadResponse(String uploadUrl, String key, String cdnUrl) {
}