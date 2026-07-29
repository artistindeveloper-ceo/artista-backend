package com.artist_in.app.config;

import java.io.InputStream;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.messaging.FirebaseMessaging;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Configuration
public class FirebaseConfig {

	@PostConstruct
	public void initialize() {
		try {
			if (FirebaseApp.getApps().isEmpty()) {
				// ✅ Classpath se load karo — jar ke andar bhi kaam karega
				InputStream serviceAccount = new ClassPathResource("firebase-service-account.json").getInputStream();

				FirebaseOptions options = FirebaseOptions.builder()
						.setCredentials(GoogleCredentials.fromStream(serviceAccount)).build();

				FirebaseApp.initializeApp(options);
				log.info("Firebase Admin SDK initialized successfully");
			}
		} catch (Exception e) {
			log.error("Firebase initialization failed", e);
		}
	}

	@Bean
	public FirebaseMessaging firebaseMessaging() {
		return FirebaseMessaging.getInstance();
	}
}