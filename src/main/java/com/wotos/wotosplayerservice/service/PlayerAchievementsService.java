package com.wotos.wotosplayerservice.service;

import com.wotos.wotosplayerservice.dao.PlayerAchievementsSnapshot;
import com.wotos.wotosplayerservice.exception.EntityNotFoundException;
import com.wotos.wotosplayerservice.repo.PlayerAchievementsSnapshotRepository;
import com.wotos.wotosplayerservice.util.feign.WotAccountsFeignClient;
import com.wotos.wotosplayerservice.util.model.PlayerAchievementsResponse;
import com.wotos.wotosplayerservice.util.model.wot.WotApiResponse;
import com.wotos.wotosplayerservice.util.model.wot.achievements.WotPlayerAchievements;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class PlayerAchievementsService {

    @Value("${env.app_id}")
    private String APP_ID;

    private final WotAccountsFeignClient wotAccountsFeignClient;
    private final AchievementMetadataService achievementMetadataService;

    private final PlayerAchievementsSnapshotRepository playerAchievementsSnapshotRepository;

    public PlayerAchievementsService(
            WotAccountsFeignClient wotAccountsFeignClient,
            AchievementMetadataService achievementMetadataService,

            PlayerAchievementsSnapshotRepository playerAchievementsSnapshotRepository
    ) {
        this.wotAccountsFeignClient = wotAccountsFeignClient;
        this.achievementMetadataService = achievementMetadataService;

        this.playerAchievementsSnapshotRepository = playerAchievementsSnapshotRepository;
    }

    /**
     * Returns a single account's live achievements from the WoT API, paired with the cached
     * achievement metadata catalogue.
     *
     * @param accountId the account to look up
     * @return the player's achievements plus achievement metadata
     * @throws EntityNotFoundException if the WoT API returns no achievements for the account
     * @throws feign.FeignException    if the upstream WoT API call fails
     */
    public PlayerAchievementsResponse getPlayerAchievements(Integer accountId) {
        WotApiResponse<Map<Integer, WotPlayerAchievements>> response =
                wotAccountsFeignClient.getPlayerAchievements(APP_ID, new Integer[]{accountId}, null, "en").getBody();

        Map<Integer, WotPlayerAchievements> data = response == null ? null : response.getData();
        WotPlayerAchievements achievements = data == null ? null : data.get(accountId);

        if (achievements == null) {
            throw new EntityNotFoundException("No achievements found for account " + accountId);
        }

        return new PlayerAchievementsResponse(
                accountId, achievements, achievementMetadataService.getAchievementMetadata());
    }

    /**
     * Returns the stored achievement-snapshot history per account.
     *
     * @param accountIds the accounts to look up
     * @return a map of account id to its stored achievement snapshots (empty list if none)
     */
    public Map<Integer, List<PlayerAchievementsSnapshot>> getPlayerAchievementsSnapshotsByAccountIds(Integer[] accountIds) {
        Map<Integer, List<PlayerAchievementsSnapshot>> playerAchievementsSnapshotsMap = new HashMap<>();

        for (Integer accountId : accountIds) {
            List<PlayerAchievementsSnapshot> playerAchievementsSnapshots =
                    playerAchievementsSnapshotRepository.findByAccountId(accountId).orElseGet(List::of);

            playerAchievementsSnapshotsMap.put(accountId, playerAchievementsSnapshots);
        }

        return playerAchievementsSnapshotsMap;
    }

    /**
     * Persists a fresh achievement snapshot row per account.
     *
     * @param accountIds the accounts to snapshot
     * @return a map of account id to the persisted snapshot
     */
    public Map<Integer, PlayerAchievementsSnapshot> createPlayerAchievementsSnapshotsByAccountIds(Integer[] accountIds) {
        Map<Integer, PlayerAchievementsSnapshot> playerAchievementsSnapshotsMap = new HashMap<>();

        for (Integer accountId : accountIds) {
            PlayerAchievementsSnapshot snapshot = new PlayerAchievementsSnapshot();
            snapshot.setAccountId(accountId);

            PlayerAchievementsSnapshot saved = playerAchievementsSnapshotRepository.save(snapshot);
            playerAchievementsSnapshotsMap.put(accountId, saved);
        }

        return playerAchievementsSnapshotsMap;
    }

}
