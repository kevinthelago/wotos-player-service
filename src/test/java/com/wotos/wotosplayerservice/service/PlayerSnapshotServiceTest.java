package com.wotos.wotosplayerservice.service;

import com.wotos.wotosplayerservice.dao.PlayerDetails;
import com.wotos.wotosplayerservice.dao.PlayerSnapshot;
import com.wotos.wotosplayerservice.exception.EntityNotFoundException;
import com.wotos.wotosplayerservice.repo.PlayerDetailsRepository;
import com.wotos.wotosplayerservice.repo.PlayerSnapshotsRepository;
import com.wotos.wotosplayerservice.util.feign.WotAccountsFeignClient;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link PlayerSnapshotService} with repositories mocked.
 */
@ExtendWith(MockitoExtension.class)
class PlayerSnapshotServiceTest {

    @Mock
    private WotAccountsFeignClient wotAccountsFeignClient;
    @Mock
    private PlayerDetailsRepository playerDetailsRepository;
    @Mock
    private PlayerSnapshotsRepository playerSnapshotsRepository;

    @InjectMocks
    private PlayerSnapshotService playerSnapshotService;

    @Test
    void getWithoutWindowReturnsFullOrderedHistory() {
        List<PlayerSnapshot> history = List.of(new PlayerSnapshot(), new PlayerSnapshot());
        when(playerSnapshotsRepository.findByAccountIdOrderByCreateTimestampAsc(1)).thenReturn(history);

        Map<Integer, List<PlayerSnapshot>> result =
                playerSnapshotService.getPlayerSnapshotsByAccountIds(new Integer[]{1}, null, null);

        assertThat(result.get(1)).isEqualTo(history);
    }

    @Test
    void getWithWindowUsesBoundedQuery() {
        List<PlayerSnapshot> windowed = List.of(new PlayerSnapshot());
        when(playerSnapshotsRepository
                .findByAccountIdAndCreateTimestampBetweenOrderByCreateTimestampAsc(1, 10L, 20L))
                .thenReturn(windowed);

        Map<Integer, List<PlayerSnapshot>> result =
                playerSnapshotService.getPlayerSnapshotsByAccountIds(new Integer[]{1}, 10L, 20L);

        assertThat(result.get(1)).isEqualTo(windowed);
    }

    @Test
    void createSnapshotPersistsFromStoredPlayer() {
        PlayerDetails details = new PlayerDetails();
        details.setAccountId(5);
        details.setNickname("eel");
        details.setGlobalRating(4200);
        when(playerDetailsRepository.findByAccountId(5)).thenReturn(Optional.of(details));
        when(playerSnapshotsRepository.save(any(PlayerSnapshot.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Map<Integer, PlayerSnapshot> result =
                playerSnapshotService.createPlayerSnapshotByAccountIds(new Integer[]{5});

        assertThat(result.get(5).getNickname()).isEqualTo("eel");
        assertThat(result.get(5).getGlobalRating()).isEqualTo(4200);
        verify(playerSnapshotsRepository).save(any(PlayerSnapshot.class));
    }

    @Test
    void createSnapshotForUnknownAccountThrows() {
        when(playerDetailsRepository.findByAccountId(eq(404))).thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                playerSnapshotService.createPlayerSnapshotByAccountIds(new Integer[]{404}))
                .isInstanceOf(EntityNotFoundException.class);
    }

}
