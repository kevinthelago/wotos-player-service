package com.wotos.wotosplayerservice.controller;

import com.wotos.wotosplayerservice.dao.PlayerAchievementsSnapshot;
import com.wotos.wotosplayerservice.service.PlayerAchievementsService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * REST endpoints for retrieving and creating {@link PlayerAchievementsSnapshot}s for a set
 * of player accounts.
 */
@RestController
@RequestMapping("/api/player")
public class PlayerAchievementsController {

    private final PlayerAchievementsService playerAchievementsService;

    public PlayerAchievementsController(PlayerAchievementsService playerAchievementsService) {
        this.playerAchievementsService = playerAchievementsService;
    }

    /**
     * Returns the stored achievement snapshots per account.
     *
     * @param accountIds the account ids to look up
     * @return a map of account id to the ordered list of {@link PlayerAchievementsSnapshot}
     */
    @GetMapping("/achievements")
    public Map<Integer, List<PlayerAchievementsSnapshot>> getPlayerAchievementsByAccountIds(
            @RequestParam("accountIds") Integer[] accountIds
    ) {
        return playerAchievementsService.getPlayerAchievementsSnapshotsByAccountIds(accountIds);
    }

    /**
     * Creates and persists a fresh achievement snapshot per account from the current WoT API.
     *
     * @param accountIds the account ids to snapshot
     * @return a map of account id to the newly-created {@link PlayerAchievementsSnapshot}
     * @throws feign.FeignException if the upstream WoT API call fails
     */
    @PostMapping("/achievements")
    public Map<Integer, PlayerAchievementsSnapshot> createPlayerAchievementsByAccountIds(
            @RequestParam("accountIds") Integer[] accountIds
    ) {
        return playerAchievementsService.createPlayerAchievementsSnapshotsByAccountIds(accountIds);
    }
}
