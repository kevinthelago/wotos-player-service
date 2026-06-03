package com.wotos.wotosplayerservice.service;

import com.wotos.wotosplayerservice.util.feign.WotAccountsFeignClient;
import com.wotos.wotosplayerservice.util.model.wot.WotApiResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link AchievementMetadataService}'s mapping of the WoT encyclopedia response.
 */
@ExtendWith(MockitoExtension.class)
class AchievementMetadataServiceTest {

    @Mock
    private WotAccountsFeignClient wotAccountsFeignClient;

    @InjectMocks
    private AchievementMetadataService achievementMetadataService;

    @Test
    void returnsMetadataFromUpstream() {
        Map<String, Object> meta = Map.of("warrior", Map.of("name", "Warrior"));
        when(wotAccountsFeignClient.getAchievementsMetadata(any(), any(), any()))
                .thenReturn(ResponseEntity.ok(new WotApiResponse<>("ok", null, null, meta)));

        assertThat(achievementMetadataService.getAchievementMetadata()).containsKey("warrior");
    }

    @Test
    void returnsEmptyMapWhenUpstreamDataNull() {
        when(wotAccountsFeignClient.getAchievementsMetadata(any(), any(), any()))
                .thenReturn(ResponseEntity.ok(new WotApiResponse<>("ok", null, null, null)));

        assertThat(achievementMetadataService.getAchievementMetadata()).isEmpty();
    }

}
