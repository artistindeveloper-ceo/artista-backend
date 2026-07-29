package com.artist_in.app.serviceimpl;

import java.time.Duration;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.artist_in.app.dto.filestorage.PresignedUploadResponse;
import com.artist_in.app.service.MediaService;

import lombok.RequiredArgsConstructor;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

@Service
@RequiredArgsConstructor

public class MediaServiceImpl implements MediaService {
	private final S3Presigner presigner;

	@Value("${aws.s3.bucket}")
	private String bucket;

	@Value("${aws.cdn.domain}")
	private String cdnDomain;

	/**
	 * mediaType: "profile" | "cover" | "post" isVideo: true → raw-videos/ folder
	 * (temp, backend processing ke liye) false → final destination directly (images
	 * ko processing nahi chahiye)
	 */
	public PresignedUploadResponse generateUploadUrl(Long userId, String mediaType, String contentType,
			boolean isVideo) {
		String ext = extensionFor(contentType);
		String key = isVideo ? "raw-videos/" + userId + "/" + UUID.randomUUID() + "." + ext
				: mediaType + "/" + userId + "/" + UUID.randomUUID() + "." + ext;

		PutObjectRequest putRequest = PutObjectRequest.builder().bucket(bucket).key(key).contentType(contentType)
				.build();

		PutObjectPresignRequest presignRequest = PutObjectPresignRequest.builder()
				.signatureDuration(Duration.ofMinutes(10)).putObjectRequest(putRequest).build();

		PresignedPutObjectRequest presigned = presigner.presignPutObject(presignRequest);
		String cdnUrl = cdnDomain + "/" + key;

		return new PresignedUploadResponse(presigned.url().toString(), key, cdnUrl);
	}

	public String cdnUrl(String key) {
		return key == null ? null : cdnDomain + "/" + key;
	}

	private String extensionFor(String contentType) {
		return switch (contentType) {
		case "image/png" -> "png";
		case "image/webp" -> "webp";
		case "video/mp4" -> "mp4";
		case "video/quicktime" -> "mov";
		default -> "jpg";
		};
	}
}
