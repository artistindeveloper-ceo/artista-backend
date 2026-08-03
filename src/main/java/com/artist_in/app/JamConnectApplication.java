package com.artist_in.app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableAsync;

import lombok.extern.slf4j.Slf4j;
@Slf4j
@SpringBootApplication
@EnableAsync
@EnableJpaAuditing
public class JamConnectApplication {

    public static void main(String[] args) {
        SpringApplication.run(JamConnectApplication.class, args);
        log.info("JamConnect application started successfully.");
    }
}
