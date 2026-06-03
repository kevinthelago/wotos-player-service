package com.wotos.wotosplayerservice.controller;

import com.wotos.wotosplayerservice.dao.PlayerSnapshot;
import com.wotos.wotosplayerservice.service.PlayerSnapshotService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * REST endpoints for retrieving and creating point-in-time {@link PlayerSnapshot}s for a set
 * of player accounts.
 */
@RestController
@RequestMapping("/api/players")
public class PlayerSnapshotController {

    private final PlayerSnapshotService playerSnapshotService;

    public PlayerSnapshotController(PlayerSnapshotService playerSnapshotService) {
        this.playerSnapshotService = playerSnapshotService;
    }

    /**
     * Returns the stored snapshot history for each account as a time series, oldest first.
     * When both {@code from} and {@code to} are supplied the series is restricted to that
     * inclusive epoch-second window.
     *
     * @param accountIds the account ids to look up
     * @param from       optional inclusive lower bound (epoch seconds)
     * @param to         optional inclusive upper bound (epoch seconds)
     * @return a map of account id to the chronologically ordered list of {@link PlayerSnapshot}
     */
    @GetMapping("/snapshots")
    public Map<Integer, List<PlayerSnapshot>> getPlayerSnapshotsByAccountIds(
            @RequestParam("accountIds") Integer[] accountIds,
            @RequestParam(value = "from", required = false) Long from,
            @RequestParam(value = "to", required = false) Long to
    ) {
        return playerSnapshotService.getPlayerSnapshotsByAccountIds(accountIds, from, to);
    }

    /**
     * Creates and persists a fresh snapshot per account from the current WoT API data.
     *
     * @param accountIds the account ids to snapshot
     * @return a map of account id to the newly-created {@link PlayerSnapshot}
     * @throws feign.FeignException if the upstream WoT API call fails
     */
    @PostMapping("/snapshots")
    public Map<Integer, PlayerSnapshot> createPlayerSnapshotsByAccountIds(
            @RequestParam("accountIds") Integer[] accountIds
    ) {
        return playerSnapshotService.createPlayerSnapshotByAccountIds(accountIds);
    }
}
