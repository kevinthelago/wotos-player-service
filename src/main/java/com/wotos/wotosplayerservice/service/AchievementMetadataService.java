package com.wotos.wotosplayerservice.service;

import com.wotos.wotosplayerservice.config.CacheConfig;
import com.wotos.wotosplayerservice.util.feign.WotAccountsFeignClient;
import com.wotos.wotosplayerservice.util.model.wot.WotApiResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Map;

/**
 * Supplies WoT achievement metadata (the achievement encyclopedia).
 *
 * <p>Lives in its own bean so the {@link Cacheable} call is routed through the Spring proxy when
 * invoked from other services — a self-invocation inside the same bean would bypass the cache.
 */
@Service
public class AchievementMetadataService {

    @Value("${env.app_id}")
    private String appId;

    private final WotAccountsFeignClient wotAccountsFeignClient;

    public AchievementMetadataService(WotAccountsFeignClient wotAccountsFeignClient) {
        this.wotAccountsFeignClient = wotAccountsFeignClient;
    }

    /**
     * Returns the achievement encyclopedia keyed by achievement id, fetched from the WoT API and
     * cached for a day (see {@link CacheConfig#ACHIEVEMENT_META_CACHE}).
     *
     * @return achievement definitions keyed by achievement id, never {@code null}
     */
    @Cacheable(CacheConfig.ACHIEVEMENT_META_CACHE)
    public Map<String, Object> getAchievementMetadata() {
        WotApiResponse<Map<String, Object>> response =
                wotAccountsFeignClient.getAchievementsMetadata(appId, null, "en").getBody();

        Map<String, Object> metadata = response == null ? null : response.getData();

        return metadata == null ? Collections.emptyMap() : metadata;
    }

}
