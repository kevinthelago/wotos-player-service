package com.wotos.wotosplayerservice.service;

import com.wotos.wotosplayerservice.dao.PlayerDetails;
import com.wotos.wotosplayerservice.dao.PlayerSnapshot;
import com.wotos.wotosplayerservice.exception.EntityNotFoundException;
import com.wotos.wotosplayerservice.repo.PlayerDetailsRepository;
import com.wotos.wotosplayerservice.repo.PlayerSnapshotsRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Integration test for snapshot persistence and retrieval against a real MySQL instance
 * provisioned by Testcontainers, verifying that {@code POST}-style creation writes one row per
 * account and that the time-bucketed read returns rows chronologically and window-filtered.
 */
@SpringBootTest
@Testcontainers(disabledWithoutDocker = true)
class PlayerSnapshotIntegrationTest {

    @Container
    static final MySQLContainer<?> MYSQL = new MySQLContainer<>("mysql:8.0")
            .withDatabaseName("wotos_players_it");

    @DynamicPropertySource
    static void datasourceProps(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", MYSQL::getJdbcUrl);
        registry.add("spring.datasource.username", MYSQL::getUsername);
        registry.add("spring.datasource.password", MYSQL::getPassword);
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "create-drop");
    }

    @Autowired
    private PlayerSnapshotService playerSnapshotService;
    @Autowired
    private PlayerDetailsRepository playerDetailsRepository;
    @Autowired
    private PlayerSnapshotsRepository playerSnapshotsRepository;

    @Test
    void createWritesOneRowPerAccountWithGeneratedId() {
        playerDetailsRepository.save(player(111, "alpha"));

        Map<Integer, PlayerSnapshot> created =
                playerSnapshotService.createPlayerSnapshotByAccountIds(new Integer[]{111});

        assertThat(created).containsKey(111);
        assertThat(created.get(111).getPlayerSnapshotId()).isNotNull();
        assertThat(playerSnapshotsRepository.findByAccountIdOrderByCreateTimestampAsc(111)).hasSize(1);
    }

    @Test
    void createForUnknownAccountThrowsEntityNotFound() {
        assertThatThrownBy(() ->
                playerSnapshotService.createPlayerSnapshotByAccountIds(new Integer[]{999999}))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void getReturnsSnapshotsTimeOrderedAndWindowed() {
        playerSnapshotsRepository.save(snapshot(222, 1_000L));
        playerSnapshotsRepository.save(snapshot(222, 3_000L));
        playerSnapshotsRepository.save(snapshot(222, 2_000L));

        List<PlayerSnapshot> all =
                playerSnapshotService.getPlayerSnapshotsByAccountIds(new Integer[]{222}, null, null).get(222);
        assertThat(all).extracting(PlayerSnapshot::getCreateTimestamp)
                .containsExactly(1_000L, 2_000L, 3_000L);

        List<PlayerSnapshot> windowed =
                playerSnapshotService.getPlayerSnapshotsByAccountIds(new Integer[]{222}, 1_500L, 2_500L).get(222);
        assertThat(windowed).extracting(PlayerSnapshot::getCreateTimestamp)
                .containsExactly(2_000L);
    }

    private static PlayerDetails player(Integer accountId, String nickname) {
        PlayerDetails details = new PlayerDetails();
        details.setAccountId(accountId);
        details.setNickname(nickname);
        details.setGlobalRating(5000);
        return details;
    }

    private static PlayerSnapshot snapshot(Integer accountId, Long createTimestamp) {
        PlayerSnapshot snapshot = new PlayerSnapshot();
        snapshot.setAccountId(accountId);
        snapshot.setCreateTimestamp(createTimestamp);
        snapshot.setNickname("nick" + accountId);
        snapshot.setGlobalRating(1234);
        return snapshot;
    }

}
