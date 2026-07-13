//package com.artist_in.app.cache.config;
//
//import java.time.Duration;
//
//import org.springframework.cache.CacheManager;
//import org.springframework.cache.annotation.EnableCaching;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.data.redis.cache.RedisCacheConfiguration;
//import org.springframework.data.redis.cache.RedisCacheManager;
//import org.springframework.data.redis.connection.RedisConnectionFactory;
//import org.springframework.data.redis.serializer.RedisSerializationContext;
//import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
//
//@Configuration
//@EnableCaching
//public class CacheConfig {
//	@Bean
//	public CacheManager cacheManager(RedisConnectionFactory redisConnectionFactory) {
//		RedisCacheConfiguration cacheConfig = RedisCacheConfiguration.defaultCacheConfig().entryTtl(Duration.ofHours(1))
//				.serializeValuesWith(RedisSerializationContext.SerializationPair
//						.fromSerializer(new GenericJackson2JsonRedisSerializer()))
//				.disableCachingNullValues();
//
//		return RedisCacheManager.builder(redisConnectionFactory).cacheDefaults(cacheConfig).build();
//	}
//}
