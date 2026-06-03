package com.wotos.wotosplayerservice.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

/**
 * Enables Spring's caching abstraction and configures the {@code achievement-meta} cache.
 *
 * <p>WoT achievement metadata (the achievement encyclopedia) is effectively static, so it is
 * cached for a day to avoid hitting the upstream WoT API on every player-achievement lookup.
 */
@Configuration
@EnableCaching
public class CacheConfig {

    public static final String ACHIEVEMENT_META_CACHE = "achievement-meta";

    @Bean
    public CacheManager cacheManager() {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager(ACHIEVEMENT_META_CACHE);
        cacheManager.setCaffeine(Caffeine.newBuilder()
                .expireAfterWrite(Duration.ofDays(1)));
        return cacheManager;
    }

}
