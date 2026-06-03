package com.wotos.wotosplayerservice.service;

import com.wotos.wotosplayerservice.dao.PlayerDetails;
import com.wotos.wotosplayerservice.dao.PlayerSnapshot;
import com.wotos.wotosplayerservice.exception.EntityNotFoundException;
import com.wotos.wotosplayerservice.repo.PlayerDetailsRepository;
import com.wotos.wotosplayerservice.repo.PlayerSnapshotsRepository;
import com.wotos.wotosplayerservice.util.feign.WotAccountsFeignClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class PlayerSnapshotService {

    @Value("${env.app_id}")
    private String APP_ID;

    private final WotAccountsFeignClient wotAccountsFeignClient;

    private final PlayerSnapshotsRepository playerSnapshotsRepository;
    private final PlayerDetailsRepository playerDetailsRepository;

    public PlayerSnapshotService(
            WotAccountsFeignClient wotAccountsFeignClient,

            PlayerDetailsRepository playerDetailsRepository,
            PlayerSnapshotsRepository playerSnapshotsRepository
    ) {
        this.wotAccountsFeignClient = wotAccountsFeignClient;

        this.playerDetailsRepository = playerDetailsRepository;
        this.playerSnapshotsRepository = playerSnapshotsRepository;
    }

    /**
     * Returns each account's snapshot history as a time series ordered oldest-first. When both
     * {@code from} and {@code to} are supplied the series is restricted to that inclusive
     * epoch-second window; otherwise the full history is returned.
     *
     * @param accountIds the accounts to look up
     * @param from       optional inclusive lower bound (epoch seconds), or {@code null}
     * @param to         optional inclusive upper bound (epoch seconds), or {@code null}
     * @return a map of account id to its chronologically ordered snapshots
     */
    public Map<Integer, List<PlayerSnapshot>> getPlayerSnapshotsByAccountIds(
            Integer[] accountIds, Long from, Long to) {
        Map<Integer, List<PlayerSnapshot>> playerSnapshotsMap = new HashMap<>();

        boolean bounded = from != null && to != null;

        for (Integer accountId : accountIds) {
            List<PlayerSnapshot> playerSnapshots = bounded
                    ? playerSnapshotsRepository
                        .findByAccountIdAndCreateTimestampBetweenOrderByCreateTimestampAsc(accountId, from, to)
                    : playerSnapshotsRepository.findByAccountIdOrderByCreateTimestampAsc(accountId);

            playerSnapshotsMap.put(accountId, playerSnapshots);
        }

        return playerSnapshotsMap;
    }

    /**
     * Builds and persists a fresh snapshot per account from its currently-stored player record.
     *
     * @param accountIds the accounts to snapshot
     * @return a map of account id to the persisted snapshot
     * @throws EntityNotFoundException if an account has no stored player record to snapshot
     */
    public Map<Integer, PlayerSnapshot> createPlayerSnapshotByAccountIds(Integer[] accountIds) {
        Map<Integer, PlayerSnapshot> playerSnapshotsMap = new HashMap<>();

        for (Integer accountId : accountIds) {
            PlayerDetails playerDetails = playerDetailsRepository.findByAccountId(accountId)
                    .orElseThrow(() -> new EntityNotFoundException(
                            "No stored player record for account " + accountId + " to snapshot"));
            PlayerSnapshot playerSnapshot = buildPlayerSnapshotFromPlayerDetails(playerDetails);

            PlayerSnapshot saved = playerSnapshotsRepository.save(playerSnapshot);
            playerSnapshotsMap.put(accountId, saved);
        }

        return playerSnapshotsMap;
    }

    private PlayerSnapshot buildPlayerSnapshotFromPlayerDetails(PlayerDetails playerDetails) {
        PlayerSnapshot playerSnapshot = new PlayerSnapshot();

        playerSnapshot.setAccountId(playerDetails.getAccountId());
        playerSnapshot.setCreateTimestamp(Instant.now().getEpochSecond());
        playerSnapshot.setNickname(playerDetails.getNickname());
        playerSnapshot.setGlobalRating(playerDetails.getGlobalRating());
        playerSnapshot.setClanId(playerDetails.getClanId());

        return playerSnapshot;
    }

}
