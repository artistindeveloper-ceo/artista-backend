package com.artist_in.app.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "artist-in.uploads")
public class UploadProperties {

	private String baseDir;
	private String baseUrl;
	private long maxImageSizeBytes;
	private long maxVideoSizeBytes;
}
