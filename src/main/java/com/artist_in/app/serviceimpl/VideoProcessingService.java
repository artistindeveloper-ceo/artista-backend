package com.artist_in.app.serviceimpl;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.artist_in.app.enums.PostStatus;
import com.artist_in.app.repository.PostRepository;
import com.artist_in.app.service.PostService;

import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

@Slf4j
@Service
public class VideoProcessingService {
	private static final String FFMPEG_PATH = "/usr/local/bin/ffmpeg";

	private final S3Client s3Client;
	private final PostRepository postRepository;
	private final PostService postService;

	public VideoProcessingService(S3Client s3Client, PostRepository postRepository, @Lazy PostService postService) {
		this.s3Client = s3Client;
		this.postRepository = postRepository;
		this.postService = postService;
	}

	@Value("${aws.s3.bucket}")
	private String bucket;

	@Value("${aws.cdn.domain}")
	private String cdnDomain;

	@Async
	public void processAsync(Long postId, String rawKey) {
		log.info("Starting async video processing: postId={}, rawKey={}", postId, rawKey);
		Path tempDir = null;
		try {
			tempDir = Files.createTempDirectory("video-" + postId);
			Path rawFile = tempDir.resolve("raw" + extOf(rawKey));
			downloadFromS3(rawKey, rawFile);

			Path compressed = tempDir.resolve("compressed.mp4");
			Path thumb = tempDir.resolve("thumb.jpg");

			boolean compressedOk = compressVideo(rawFile, compressed);
			Path finalVideo = compressedOk ? compressed : rawFile;
			boolean thumbOk = generateThumbnail(finalVideo, thumb);

			String videoKey = "posts/" + postId + "/" + UUID.randomUUID() + ".mp4";
			uploadToS3(finalVideo, videoKey);
			s3Client.deleteObject(b -> b.bucket(bucket).key(rawKey)); // raw cleanup

			String thumbKey = null;
			if (thumbOk) {
				thumbKey = "posts/" + postId + "/" + UUID.randomUUID() + "_thumb.jpg";
				uploadToS3(thumb, thumbKey);
			}

//			Post post = postRepository.findById(postId).orElseThrow();
//			post.setMediaUrl(cdnDomain + "/" + videoKey);
//			post.setThumbnailUrl(thumbKey != null ? cdnDomain + "/" + thumbKey : null);
//			post.setStatus(PostStatus.READY);
//			postRepository.save(post);
//			log.info("Video processing complete: postId={}", postId);

			String mediaUrl = cdnDomain + "/" + videoKey;
			String thumbnailUrl = thumbKey != null ? cdnDomain + "/" + thumbKey : null;
			postService.markVideoReady(postId, mediaUrl, thumbnailUrl);
			log.info("Video processing complete: postId={}", postId);
		} catch (Exception ex) {
			log.error("Video processing failed for postId={}", postId, ex);
			postRepository.findById(postId).ifPresent(p -> {
				p.setStatus(PostStatus.FAILED);
				postRepository.save(p);
			});
		} finally {
			if (tempDir != null)
				deleteRecursively(tempDir);
		}
	}

	private void downloadFromS3(String key, Path dest) throws Exception {
		try (InputStream in = s3Client.getObject(GetObjectRequest.builder().bucket(bucket).key(key).build())) {
			Files.copy(in, dest, StandardCopyOption.REPLACE_EXISTING);
		}
	}

	private void uploadToS3(Path file, String key) {
		s3Client.putObject(PutObjectRequest.builder().bucket(bucket).key(key).build(), RequestBody.fromFile(file));
	}

	private boolean compressVideo(Path input, Path output) {
		return runFfmpeg(FFMPEG_PATH, "-i", input.toString(), "-vf", "scale=-2:'min(720,ih)'", "-c:v", "libx264",
				"-preset", "fast", "-crf", "26", "-maxrate", "1500k", "-bufsize", "3000k", "-c:a", "aac", "-b:a",
				"128k", "-movflags", "+faststart", "-y", output.toString());
	}

	private boolean generateThumbnail(Path video, Path thumbOut) {
		return runFfmpeg(FFMPEG_PATH, "-i", video.toString(), "-ss", "00:00:00.5", "-vframes", "1", "-vf",
				"scale=480:-1", "-y", thumbOut.toString());
	}

	private boolean runFfmpeg(String... command) {
		try {
			ProcessBuilder pb = new ProcessBuilder(command);
			pb.redirectErrorStream(true);
			Process process = pb.start();
			try (InputStream is = process.getInputStream()) {
				is.readAllBytes();
			}
			boolean finished = process.waitFor(120, TimeUnit.SECONDS);
			if (!finished) {
				process.destroyForcibly();
				return false;
			}
			return process.exitValue() == 0;
		} catch (Exception ex) {
			log.error("FFmpeg command failed", ex);
			return false;
		}
	}

	private String extOf(String key) {
		int i = key.lastIndexOf('.');
		return i == -1 ? ".mp4" : key.substring(i);
	}

	private void deleteRecursively(Path dir) {
		try (var walk = Files.walk(dir)) {
			walk.sorted((a, b) -> b.compareTo(a)).forEach(p -> {
				try {
					Files.deleteIfExists(p);
				} catch (Exception ignored) {
				}
			});
		} catch (Exception ignored) {
		}
	}
}
