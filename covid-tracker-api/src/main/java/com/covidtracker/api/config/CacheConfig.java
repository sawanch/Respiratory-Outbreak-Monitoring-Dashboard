package com.covidtracker.api.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Configuration;

/**
 * Cache Configuration - Enables Spring's caching framework via @EnableCaching
 * 
 * Separated from RedisConfig to avoid bean dependency validation errors when Redis is disabled.
 * When enabled (spring.cache.redis.enabled=true), activates @Cacheable annotations.
 * When disabled, caching is bypassed and all requests hit the database directly.
 * 
 * Note: Intentionally blank - actual cache implementation is in RedisConfig.java
 */
@Configuration
@EnableCaching
@ConditionalOnProperty(name = "spring.cache.redis.enabled", havingValue = "true")
public class CacheConfig {
    // Intentionally blank - only enables caching framework
    // Actual cache implementation provided by RedisConfig.java
}

