package com.artist_in.app.serviceimpl;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Set;
import java.util.UUID;

import com.artist_in.app.service.FileStorageService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import com.artist_in.app.config.UploadProperties;
import com.artist_in.app.enums.MediaType;
import com.artist_in.app.exception.BadRequestException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class FileStorageServiceImpl implements FileStorageService {

	private static final Set<String> ALLOWED_IMAGE_EXTENSIONS = Set.of("jpg", "jpeg", "png", "gif", "webp");
	private static final Set<String> ALLOWED_VIDEO_EXTENSIONS = Set.of("mp4", "mov", "webm", "m4v");
	private static final String FFMPEG_PATH = "/usr/local/bin/ffmpeg";

	private final UploadProperties uploadProperties;


	/**
	 * Stores a file on local disk under uploads/{category}/{uuid}.{ext} and returns
	 * its public URL.
	 */
	@Override
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

	/**
	 * Stores raw video, then compresses it (H.264, max 720p) and generates a JPEG
	 * thumbnail via FFmpeg. Returns the compressed video URL + thumbnail URL.
	 */
	@Override
	public VideoStoreResult storeVideo(MultipartFile file, UploadCategory category) {
		validateNotEmpty(file);
		String extension = extractExtension(file.getOriginalFilename());
		if (!ALLOWED_VIDEO_EXTENSIONS.contains(extension.toLowerCase())) {
			throw new BadRequestException("Unsupported video type. Allowed: " + ALLOWED_VIDEO_EXTENSIONS);
		}
		if (file.getSize() > uploadProperties.getMaxVideoSizeBytes()) {
			throw new BadRequestException("Video exceeds the maximum allowed size.");
		}

		try {
			Path categoryDir = Paths.get(uploadProperties.getBaseDir(), category.name().toLowerCase());
			Files.createDirectories(categoryDir);

			// 1. Raw upload ko temp file mein save karo
			String rawFilename = UUID.randomUUID() + "_raw." + extension;
			Path rawPath = categoryDir.resolve(rawFilename);
			try (InputStream in = file.getInputStream()) {
				Files.copy(in, rawPath, StandardCopyOption.REPLACE_EXISTING);
			}

			// 2. Compress karo (H.264, max 720p height, reasonable bitrate)
			String compressedFilename = UUID.randomUUID() + ".mp4";
			Path compressedPath = categoryDir.resolve(compressedFilename);
			boolean compressed = compressVideo(rawPath, compressedPath);

			// Agar compression fail ho jaye (edge case), raw file hi use karo fallback ke
			// taur pe
			Path finalVideoPath;
			String finalVideoFilename;
			if (compressed && Files.exists(compressedPath) && Files.size(compressedPath) > 0) {
				finalVideoPath = compressedPath;
				finalVideoFilename = compressedFilename;
				Files.deleteIfExists(rawPath); // raw ab zaroorat nahi
			} else {
				log.warn("Video compression failed, falling back to raw upload for: {}", rawFilename);
				finalVideoPath = rawPath;
				finalVideoFilename = rawFilename;
			}

			// 3. Thumbnail generate karo (0.5 sec ka frame, 480px width)
			String thumbFilename = UUID.randomUUID() + "_thumb.jpg";
			Path thumbPath = categoryDir.resolve(thumbFilename);
			boolean thumbGenerated = generateThumbnail(finalVideoPath, thumbPath);

			String videoRelativePath = category.name().toLowerCase() + "/" + finalVideoFilename;
			String videoUrl = uploadProperties.getBaseUrl() + "/" + videoRelativePath;

			String thumbnailUrl = null;
			if (thumbGenerated && Files.exists(thumbPath)) {
				String thumbRelativePath = category.name().toLowerCase() + "/" + thumbFilename;
				thumbnailUrl = uploadProperties.getBaseUrl() + "/" + thumbRelativePath;
			}

			return new VideoStoreResult(videoUrl, thumbnailUrl);
		} catch (IOException ex) {
			throw new RuntimeException("Failed to store uploaded video.", ex);
		}
	}

	/** Compresses video to H.264 MP4, capped at 720p height, ~1.5 Mbps bitrate. */
	private boolean compressVideo(Path input, Path output) {
		try {
			ProcessBuilder pb = new ProcessBuilder(FFMPEG_PATH, "-i", input.toAbsolutePath().toString(), "-vf",
					"scale=-2:'min(720,ih)'", "-c:v", "libx264", "-preset", "fast", "-crf", "26", "-maxrate", "1500k",
					"-bufsize", "3000k", "-c:a", "aac", "-b:a", "128k", "-movflags", "+faststart", "-y",
					output.toAbsolutePath().toString());
			pb.redirectErrorStream(true);
			Process process = pb.start();

			// FFmpeg output ko drain karo taaki process block na ho
			try (InputStream is = process.getInputStream()) {
				is.readAllBytes();
			}

			boolean finished = process.waitFor(120, java.util.concurrent.TimeUnit.SECONDS);
			if (!finished) {
				process.destroyForcibly();
				log.error("FFmpeg compression timed out for: {}", input);
				return false;
			}
			return process.exitValue() == 0;
		} catch (IOException | InterruptedException ex) {
			log.error("FFmpeg compression failed for: {}", input, ex);
			return false;
		}
	}

	/** Extracts a single JPEG frame at 0.5s, scaled to 480px width. */
	private boolean generateThumbnail(Path videoPath, Path thumbOutput) {
		try {
			ProcessBuilder pb = new ProcessBuilder(FFMPEG_PATH, "-i", videoPath.toAbsolutePath().toString(), "-ss",
					"00:00:00.5", "-vframes", "1", "-vf", "scale=480:-1", "-y",
					thumbOutput.toAbsolutePath().toString());
			pb.redirectErrorStream(true);
			Process process = pb.start();

			try (InputStream is = process.getInputStream()) {
				is.readAllBytes();
			}

			boolean finished = process.waitFor(30, java.util.concurrent.TimeUnit.SECONDS);
			if (!finished) {
				process.destroyForcibly();
				log.error("Thumbnail generation timed out for: {}", videoPath);
				return false;
			}
			return process.exitValue() == 0;
		} catch (IOException | InterruptedException ex) {
			log.error("Thumbnail generation failed for: {}", videoPath, ex);
			return false;
		}
	}

	/**
	 * Accepts either an image or a video, auto-detecting by extension; used for
	 * generic post media uploads.
	 */
	@Override
	public StoredMedia storeMedia(MultipartFile file, UploadCategory category) {
		validateNotEmpty(file);
		String extension = extractExtension(file.getOriginalFilename()).toLowerCase();

		if (ALLOWED_IMAGE_EXTENSIONS.contains(extension)) {
			return new StoredMedia(storeImage(file, category), null, MediaType.IMAGE);
		} else if (ALLOWED_VIDEO_EXTENSIONS.contains(extension)) {
			VideoStoreResult result = storeVideo(file, category);
			return new StoredMedia(result.videoUrl(), result.thumbnailUrl(), MediaType.VIDEO);
		} else {
			throw new BadRequestException("Unsupported file type. Allowed images: " + ALLOWED_IMAGE_EXTENSIONS
					+ ", allowed videos: " + ALLOWED_VIDEO_EXTENSIONS);
		}
	}



	@Override
	public Path resolvePath(String relativePath) {
		return Paths.get(uploadProperties.getBaseDir()).resolve(relativePath).normalize();
	}
	@Override
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