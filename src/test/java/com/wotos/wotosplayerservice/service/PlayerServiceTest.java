package com.wotos.wotosplayerservice.service;

import com.wotos.wotosplayerservice.dao.PlayerDetails;
import com.wotos.wotosplayerservice.repo.PlayerAchievementsSnapshotRepository;
import com.wotos.wotosplayerservice.repo.PlayerDetailsRepository;
import com.wotos.wotosplayerservice.util.feign.WotAccountsFeignClient;
import com.wotos.wotosplayerservice.util.feign.WotPlayerVehiclesFeignClient;
import com.wotos.wotosplayerservice.util.model.wot.WotApiResponse;
import com.wotos.wotosplayerservice.util.model.wot.player.WotPlayer;
import com.wotos.wotosplayerservice.util.model.wot.player.WotPlayerDetails;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link PlayerService} with the WoT Feign clients and JPA repositories mocked,
 * so the service's mapping and persistence orchestration is verified without a running context.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class PlayerServiceTest {

    @Mock
    private WotAccountsFeignClient wotAccountsFeignClient;
    @Mock
    private WotPlayerVehiclesFeignClient playerVehiclesFeignClient;
    @Mock
    private PlayerAchievementsSnapshotRepository playerAchievementsSnapshotRepository;
    @Mock
    private PlayerDetailsRepository playerDetailsRepository;

    @InjectMocks
    private PlayerService playerService;

    @Test
    void getPlayersByNicknameReturnsUpstreamData() {
        WotPlayer player = org.mockito.Mockito.mock(WotPlayer.class);
        when(wotAccountsFeignClient.getPlayersByExactNickname(any(), any(), any(), any(), any()))
                .thenReturn(ResponseEntity.ok(new WotApiResponse<>("ok", null, null, List.of(player))));

        List<WotPlayer> result = playerService.getPlayersByNickname(
                new String[]{"foo"}, "en", 100, "exact");

        assertThat(result).containsExactly(player);
    }

    @Test
    void getPlayersMapByAccountIdsMapsStoredAndMissing() {
        PlayerDetails stored = player(1, "alpha");
        when(playerDetailsRepository.findByAccountId(1)).thenReturn(Optional.of(stored));
        when(playerDetailsRepository.findByAccountId(2)).thenReturn(Optional.empty());

        Map<Integer, PlayerDetails> result =
                playerService.getPlayersMapByAccountIds(new Integer[]{1, 2});

        assertThat(result.get(1)).isEqualTo(stored);
        assertThat(result.get(2)).isNull();
    }

    @Test
    void createPlayersByAccountIdsPersistsOnlyNewAccounts() {
        WotPlayerDetails wot = wotDetails(7, "newbie");
        when(wotAccountsFeignClient.getPlayerDetails(any(), any(), any(), any(), any(), any()))
                .thenReturn(ResponseEntity.ok(new WotApiResponse<>("ok", null, null, Map.of(7, wot))));
        when(playerDetailsRepository.findByAccountId(7)).thenReturn(Optional.empty());

        Map<Integer, PlayerDetails> result =
                playerService.createPlayersByAccountIds(new Integer[]{7});

        assertThat(result.get(7).getNickname()).isEqualTo("newbie");
        verify(playerDetailsRepository).save(any(PlayerDetails.class));
    }

    @Test
    void createPlayersByAccountIdsKeepsExistingWithoutSaving() {
        PlayerDetails existing = player(7, "veteran");
        WotPlayerDetails wot = wotDetails(7, "veteran");
        when(wotAccountsFeignClient.getPlayerDetails(any(), any(), any(), any(), any(), any()))
                .thenReturn(ResponseEntity.ok(new WotApiResponse<>("ok", null, null, Map.of(7, wot))));
        when(playerDetailsRepository.findByAccountId(7)).thenReturn(Optional.of(existing));

        Map<Integer, PlayerDetails> result =
                playerService.createPlayersByAccountIds(new Integer[]{7});

        assertThat(result.get(7)).isEqualTo(existing);
        verify(playerDetailsRepository, never()).save(any(PlayerDetails.class));
    }

    @Test
    void updatePlayersByAccountIdAppliesUpstreamFields() {
        PlayerDetails existing = player(9, "old");
        WotPlayerDetails wot = wotDetails(9, "renamed");
        when(wotAccountsFeignClient.getPlayerDetails(any(), any(), any(), any(), any(), any()))
                .thenReturn(ResponseEntity.ok(new WotApiResponse<>("ok", null, null, Map.of(9, wot))));
        when(playerDetailsRepository.findByAccountId(9)).thenReturn(Optional.of(existing));

        playerService.updatePlayersByAccountId(new Integer[]{9});

        assertThat(existing.getNickname()).isEqualTo("renamed");
        verify(playerDetailsRepository).save(existing);
    }

    @Test
    void havePlayersBeenUpdatedComparesTimestamps() {
        WotPlayerDetails wot = wotDetails(3, "p3");
        when(wot.getUpdatedAt()).thenReturn(100);
        when(wotAccountsFeignClient.getPlayerDetails(any(), any(), any(), any(), any(), any()))
                .thenReturn(ResponseEntity.ok(new WotApiResponse<>("ok", null, null, Map.of(3, wot))));
        when(playerDetailsRepository.findUpdatedAtByAccountId(3)).thenReturn(Optional.of(200L));

        Map<Integer, Boolean> result =
                playerService.havePlayersBeenUpdated(new Integer[]{3});

        assertThat(result.get(3)).isTrue();
    }

    private static PlayerDetails player(Integer accountId, String nickname) {
        PlayerDetails details = new PlayerDetails();
        details.setAccountId(accountId);
        details.setNickname(nickname);
        return details;
    }

    private static WotPlayerDetails wotDetails(Integer accountId, String nickname) {
        WotPlayerDetails wot = org.mockito.Mockito.mock(WotPlayerDetails.class);
        when(wot.getAccountId()).thenReturn(accountId);
        when(wot.getNickname()).thenReturn(nickname);
        return wot;
    }

}
