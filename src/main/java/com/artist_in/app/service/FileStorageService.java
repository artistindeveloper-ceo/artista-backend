package com.artist_in.app.service;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Set;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import com.artist_in.app.config.UploadProperties;
import com.artist_in.app.enums.MediaType;
import com.artist_in.app.exception.BadRequestException;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class FileStorageService {

	private static final Set<String> ALLOWED_IMAGE_EXTENSIONS = Set.of("jpg", "jpeg", "png", "gif", "webp");
	private static final Set<String> ALLOWED_VIDEO_EXTENSIONS = Set.of("mp4", "mov", "webm", "m4v");

	private final UploadProperties uploadProperties;

	public enum UploadCategory {
		PROFILE_PHOTOS, COVER_PHOTOS, POST_MEDIA, CHAT_ATTACHMENTS
	}

	/**
	 * Stores a file on local disk under uploads/{category}/{uuid}.{ext} and returns
	 * its public URL.
	 */
	public String storeImage(MultipartFile file, UploadCategory category) {
		validateNotEmpty(file);
		String extension = extractExtension(file.getOriginalFilename());
		if (!ALLOWED_IMAGE_EXTENSIONS.contains(extension.toLowerCase())) {
			throw new BadRequestException("Unsupported image type. Allowed: " + ALLOWED_IMAGE_EXTENSIONS);
		}
		if (file.getSize() > uploadProperties.getMaxImageSizeBytes()) {
			throw new BadRequestException("Image exceeds the maximum allowed size.");
		}
		return store(file, category, extension);
	}

	public String storeVideo(MultipartFile file, UploadCategory category) {
		validateNotEmpty(file);
		String extension = extractExtension(file.getOriginalFilename());
		if (!ALLOWED_VIDEO_EXTENSIONS.contains(extension.toLowerCase())) {
			throw new BadRequestException("Unsupported video type. Allowed: " + ALLOWED_VIDEO_EXTENSIONS);
		}
		if (file.getSize() > uploadProperties.getMaxVideoSizeBytes()) {
			throw new BadRequestException("Video exceeds the maximum allowed size.");
		}
		return store(file, category, extension);
	}

	/**
	 * Accepts either an image or a video, auto-detecting by extension; used for
	 * generic post media uploads.
	 */
	public StoredMedia storeMedia(MultipartFile file, UploadCategory category) {
		validateNotEmpty(file);
		String extension = extractExtension(file.getOriginalFilename()).toLowerCase();

		if (ALLOWED_IMAGE_EXTENSIONS.contains(extension)) {
			return new StoredMedia(storeImage(file, category), MediaType.IMAGE);
		} else if (ALLOWED_VIDEO_EXTENSIONS.contains(extension)) {
			return new StoredMedia(storeVideo(file, category), MediaType.VIDEO);
		} else {
			throw new BadRequestException("Unsupported file type. Allowed images: " + ALLOWED_IMAGE_EXTENSIONS
					+ ", allowed videos: " + ALLOWED_VIDEO_EXTENSIONS);
		}
	}

	public record StoredMedia(String url, MediaType mediaType) {
	}

	public Path resolvePath(String relativePath) {
		return Paths.get(uploadProperties.getBaseDir()).resolve(relativePath).normalize();
	}

	public InputStream readFile(String relativePath) throws IOException {
		Path path = resolvePath(relativePath);
		if (!Files.exists(path)) {
			throw new java.io.FileNotFoundException("File not found: " + relativePath);
		}
		return Files.newInputStream(path);
	}

	private String store(MultipartFile file, UploadCategory category, String extension) {
		try {
			Path categoryDir = Paths.get(uploadProperties.getBaseDir(), category.name().toLowerCase());
			Files.createDirectories(categoryDir);

			String filename = UUID.randomUUID() + "." + extension;
			Path destination = categoryDir.resolve(filename);

			try (InputStream in = file.getInputStream()) {
				Files.copy(in, destination, StandardCopyOption.REPLACE_EXISTING);
			}

			String relativePath = category.name().toLowerCase() + "/" + filename;
			return uploadProperties.getBaseUrl() + "/" + relativePath;
		} catch (IOException ex) {
			throw new RuntimeException("Failed to store uploaded file.", ex);
		}
	}

	private void validateNotEmpty(MultipartFile file) {
		if (file == null || file.isEmpty()) {
			throw new BadRequestException("Uploaded file is empty.");
		}
	}

	private String extractExtension(String originalFilename) {
		if (!StringUtils.hasText(originalFilename) || !originalFilename.contains(".")) {
			throw new BadRequestException("Uploaded file must have a valid extension.");
		}
		return originalFilename.substring(originalFilename.lastIndexOf('.') + 1);
	}
}
