package com.wotos.wotosplayerservice.controller;

import com.wotos.wotosplayerservice.dao.PlayerAchievementsSnapshot;
import com.wotos.wotosplayerservice.service.PlayerAchievementsService;
import com.wotos.wotosplayerservice.util.model.PlayerAchievementsResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * REST endpoints for player achievements: live lookup against the WoT API (enriched with cached
 * achievement metadata) plus retrieval and creation of stored {@link PlayerAchievementsSnapshot}s.
 */
@RestController
@RequestMapping("/api/players")
public class PlayerAchievementsController {

    private final PlayerAchievementsService playerAchievementsService;

    public PlayerAchievementsController(PlayerAchievementsService playerAchievementsService) {
        this.playerAchievementsService = playerAchievementsService;
    }

    /**
     * Returns a single account's live achievements from the WoT API, paired with the cached
     * achievement metadata catalogue.
     *
     * @param accountId the account id to look up
     * @return the player's achievements and the achievement metadata
     * @throws com.wotos.wotosplayerservice.exception.EntityNotFoundException if the account has no achievements
     * @throws feign.FeignException if the upstream WoT API call fails
     */
    @GetMapping("/{accountId}/achievements")
    public PlayerAchievementsResponse getPlayerAchievements(
            @PathVariable("accountId") Integer accountId
    ) {
        return playerAchievementsService.getPlayerAchievements(accountId);
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
     * Creates and persists a fresh achievement snapshot per account.
     *
     * @param accountIds the account ids to snapshot
     * @return a map of account id to the newly-created {@link PlayerAchievementsSnapshot}
     */
    @PostMapping("/achievements")
    public Map<Integer, PlayerAchievementsSnapshot> createPlayerAchievementsByAccountIds(
            @RequestParam("accountIds") Integer[] accountIds
    ) {
        return playerAchievementsService.createPlayerAchievementsSnapshotsByAccountIds(accountIds);
    }
}
