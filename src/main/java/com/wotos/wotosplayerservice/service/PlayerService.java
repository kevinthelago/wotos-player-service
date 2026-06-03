package com.wotos.wotosplayerservice.service;

import com.wotos.wotosplayerservice.dao.PlayerDetails;
import com.wotos.wotosplayerservice.repo.PlayerAchievementsSnapshotRepository;
import com.wotos.wotosplayerservice.repo.PlayerDetailsRepository;
import com.wotos.wotosplayerservice.util.feign.WotAccountsFeignClient;
import com.wotos.wotosplayerservice.util.feign.WotPlayerVehiclesFeignClient;
import com.wotos.wotosplayerservice.util.model.wot.player.WotPlayer;
import com.wotos.wotosplayerservice.util.model.wot.player.WotPlayerDetails;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class PlayerService {

    @Value("${env.app_id}")
    private String APP_ID;

    private final WotAccountsFeignClient wotAccountsFeignClient;
    private final WotPlayerVehiclesFeignClient playerVehiclesFeignClient;

    private final PlayerAchievementsSnapshotRepository playerAchievementsSnapshotRepository;
    private final PlayerDetailsRepository playerDetailsRepository;

    public PlayerService(
            WotAccountsFeignClient wotAccountsFeignClient,
            WotPlayerVehiclesFeignClient playerVehiclesFeignClient,

            PlayerAchievementsSnapshotRepository playerAchievementsSnapshotRepository,
            PlayerDetailsRepository playerDetailsRepository
    ) {
        this.wotAccountsFeignClient = wotAccountsFeignClient;
        this.playerVehiclesFeignClient = playerVehiclesFeignClient;

        this.playerAchievementsSnapshotRepository = playerAchievementsSnapshotRepository;
        this.playerDetailsRepository = playerDetailsRepository;
    }

    public List<WotPlayer> getPlayersByNickname(String[] nicknames, String language, Integer limit, String searchType) {
        return fetchPlayersByNickname(nicknames, language, limit, searchType);
    }

    public Map<Integer, PlayerDetails> getPlayersMapByAccountIds(Integer[] accountIds) {
        Map<Integer, PlayerDetails> playerMap = new HashMap<>();

        for (Integer accountId : accountIds) {
            PlayerDetails playerDetails = playerDetailsRepository.findByAccountId(accountId).orElse(null);

            playerMap.put(accountId, playerDetails);
        }

        return playerMap;
    }

    public Map<Integer, PlayerDetails> createPlayersByAccountIds(Integer[] accountIds) {
        Map<Integer, PlayerDetails> playerMap = new HashMap<>();
        String[] fields = {
                "account_id", "nickname", "client_language", "last_battle_time",
                "created_at", "updated_at", "global_rating", "clan_id", "logout_at"
        };
        Map<Integer, WotPlayerDetails> wotPlayerDetailsMap = fetchPlayerDetailsMap(null, null, fields, "en", accountIds);

        for (Integer accountId : accountIds) {
            PlayerDetails playerDetails = playerDetailsRepository.findByAccountId(accountId).orElse(null);

            if (playerDetails == null) {
                WotPlayerDetails wotPlayerDetails = wotPlayerDetailsMap.get(accountId);
                PlayerDetails newPlayerDetails = buildPlayerDetailsFromWotPlayerDetails(wotPlayerDetails);

                playerDetailsRepository.save(newPlayerDetails);
                playerMap.put(accountId, newPlayerDetails);
            } else {
                playerMap.put(accountId, playerDetails);
            }
        }

        return playerMap;
    }

    public Map<Integer, PlayerDetails> updatePlayersByAccountId(Integer[] accountIds) {
        Map<Integer, PlayerDetails> playerMap = new HashMap<>();
        String[] fields = {
                "nickname", "client_language", "last_battle_time",
                "updated_at", "global_rating", "clan_id", "logout_at"
        };
        Map<Integer, WotPlayerDetails> wotPlayerDetailsMap = fetchPlayerDetailsMap(null, null, fields, "", accountIds);

        for (Integer accountId : accountIds) {
            WotPlayerDetails wotPlayerDetails = wotPlayerDetailsMap.get(accountId);

            playerDetailsRepository.findByAccountId(accountId).ifPresent(playerDetails -> {
                  playerDetails.setNickname(wotPlayerDetails.getNickname());
                  playerDetails.setClientLanguage(wotPlayerDetails.getClientLanguage());
                  playerDetails.setLastBattleTime(wotPlayerDetails.getLastBattleTime());
                  playerDetails.setUpdatedAt(wotPlayerDetails.getUpdatedAt());
                  playerDetails.setGlobalRating(wotPlayerDetails.getGlobalRating());
                  playerDetails.setClanId(wotPlayerDetails.getClanId());
                  playerDetails.setLogoutAt(wotPlayerDetails.getLogoutAt());

                  playerDetailsRepository.save(playerDetails);
            });

            PlayerDetails playerDetails = playerDetailsRepository.findByAccountId(accountId).orElse(null);
            playerMap.put(accountId, playerDetails);
        }

        return playerMap;
    }

    public Map<Integer, Boolean> havePlayersBeenUpdated(Integer[] accountIds) {
        Map<Integer, Boolean> playerUpdatedMap = new HashMap<>();
        String[] fields = {"updated_at"};
        Map<Integer, WotPlayerDetails> wotPlayerDetailsMap = fetchPlayerDetailsMap("", null, fields, "", accountIds);

        for (Integer accountId : accountIds) {
            Long lastUpdateAt = playerDetailsRepository.findUpdatedAtByAccountId(accountId).orElse(Long.MAX_VALUE);

            if (lastUpdateAt - wotPlayerDetailsMap.get(accountId).getUpdatedAt() > 0) {
                playerUpdatedMap.put(accountId, Boolean.TRUE);
            } else {
                playerUpdatedMap.put(accountId, Boolean.FALSE);
            }
        }

        return playerUpdatedMap;
    }

    private PlayerDetails buildPlayerDetailsFromWotPlayerDetails(WotPlayerDetails wotPlayerDetails) {
        PlayerDetails playerDetails = new PlayerDetails();

        playerDetails.setAccountId(wotPlayerDetails.getAccountId());
        playerDetails.setNickname(wotPlayerDetails.getNickname());
        playerDetails.setClientLanguage(wotPlayerDetails.getClientLanguage());
        playerDetails.setLastBattleTime(wotPlayerDetails.getLastBattleTime());
        playerDetails.setCreatedAt(wotPlayerDetails.getCreatedAt());
        playerDetails.setUpdatedAt(wotPlayerDetails.getUpdatedAt());
        playerDetails.setGlobalRating(wotPlayerDetails.getGlobalRating());
        playerDetails.setClanId(wotPlayerDetails.getClanId());
        playerDetails.setLogoutAt(wotPlayerDetails.getLogoutAt());

        return playerDetails;
    }

    private Map<Integer, WotPlayerDetails> fetchPlayerDetailsMap(String accessToken, String[] extras, String[] fields, String language, Integer[] accountIds) {
        Map<Integer, WotPlayerDetails> wotPlayerDetailsMap = wotAccountsFeignClient.getPlayerDetails(
                APP_ID, accessToken, extras, fields, language, accountIds
        ).getBody().getData();

        return wotPlayerDetailsMap;
    }

    private List<WotPlayer> fetchPlayersByNickname(String[] nicknames, String language, Integer limit, String searchType) {
        return wotAccountsFeignClient.getPlayersByExactNickname(
                APP_ID, nicknames, language, limit, searchType
        ).getBody().getData();
    }

}
