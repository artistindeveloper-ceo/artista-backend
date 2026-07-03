package com.artist_in.app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class JamConnectApplication {

    public static void main(String[] args) {
        SpringApplication.run(JamConnectApplication.class, args);
        System.out.println("Jam is connected Now.............");
    }
}
