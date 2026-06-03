package com.wotos.wotosplayerservice.service;

import com.wotos.wotosplayerservice.dao.PlayerAchievementsSnapshot;
import com.wotos.wotosplayerservice.exception.EntityNotFoundException;
import com.wotos.wotosplayerservice.repo.PlayerAchievementsSnapshotRepository;
import com.wotos.wotosplayerservice.util.feign.WotAccountsFeignClient;
import com.wotos.wotosplayerservice.util.model.PlayerAchievementsResponse;
import com.wotos.wotosplayerservice.util.model.wot.WotApiResponse;
import com.wotos.wotosplayerservice.util.model.wot.achievements.WotPlayerAchievements;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link PlayerAchievementsService} with the Feign client, metadata service, and
 * repository mocked.
 */
@ExtendWith(MockitoExtension.class)
class PlayerAchievementsServiceTest {

    @Mock
    private WotAccountsFeignClient wotAccountsFeignClient;
    @Mock
    private AchievementMetadataService achievementMetadataService;
    @Mock
    private PlayerAchievementsSnapshotRepository playerAchievementsSnapshotRepository;

    @InjectMocks
    private PlayerAchievementsService playerAchievementsService;

    @Test
    void getPlayerAchievementsPairsLiveDataWithMetadata() {
        WotPlayerAchievements achievements = new WotPlayerAchievements(null, null, null);
        when(wotAccountsFeignClient.getPlayerAchievements(any(), any(), any(), any()))
                .thenReturn(ResponseEntity.ok(new WotApiResponse<>("ok", null, null, Map.of(42, achievements))));
        when(achievementMetadataService.getAchievementMetadata()).thenReturn(Map.of("warrior", "meta"));

        PlayerAchievementsResponse response = playerAchievementsService.getPlayerAchievements(42);

        assertThat(response.accountId()).isEqualTo(42);
        assertThat(response.achievements()).isSameAs(achievements);
        assertThat(response.metadata()).containsKey("warrior");
    }

    @Test
    void getPlayerAchievementsThrowsWhenAccountAbsent() {
        when(wotAccountsFeignClient.getPlayerAchievements(any(), any(), any(), any()))
                .thenReturn(ResponseEntity.ok(new WotApiResponse<>("ok", null, null, Map.of())));

        assertThatThrownBy(() -> playerAchievementsService.getPlayerAchievements(42))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void getSnapshotsReturnsEmptyListWhenNonePresent() {
        when(playerAchievementsSnapshotRepository.findByAccountId(7)).thenReturn(Optional.empty());

        Map<Integer, List<PlayerAchievementsSnapshot>> result =
                playerAchievementsService.getPlayerAchievementsSnapshotsByAccountIds(new Integer[]{7});

        assertThat(result.get(7)).isEmpty();
    }

    @Test
    void createSnapshotsPersistsOneRowPerAccount() {
        when(playerAchievementsSnapshotRepository.save(any(PlayerAchievementsSnapshot.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Map<Integer, PlayerAchievementsSnapshot> result =
                playerAchievementsService.createPlayerAchievementsSnapshotsByAccountIds(new Integer[]{7, 8});

        assertThat(result).containsOnlyKeys(7, 8);
        assertThat(result.get(7).getAccountId()).isEqualTo(7);
        verify(playerAchievementsSnapshotRepository, org.mockito.Mockito.times(2))
                .save(any(PlayerAchievementsSnapshot.class));
    }

}
