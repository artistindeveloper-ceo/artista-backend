package com.artist_in.app.cache.config;

import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableCaching
public class SpringCacheConfig {
    @Bean
    public CacheManager cacheManager() {
        ConcurrentMapCacheManager cacheManager =
                new ConcurrentMapCacheManager(
                        "feed",
                        "explore",
                        "userPosts",
                        "post",
                        "userInstruments"   // ← ये line जोड़ दें
                );
        cacheManager.setAllowNullValues(false);
        return cacheManager;
    }
}