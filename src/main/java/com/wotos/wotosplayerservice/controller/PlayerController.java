package com.wotos.wotosplayerservice.controller;

import com.wotos.wotosplayerservice.dao.PlayerDetails;
import com.wotos.wotosplayerservice.service.PlayerService;
import com.wotos.wotosplayerservice.util.model.wot.player.WotPlayer;
import com.wotos.wotosplayerservice.validation.constraints.Language;
import com.wotos.wotosplayerservice.validation.constraints.PlayerSearch;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.constraints.Max;
import java.util.List;
import java.util.Map;

/**
 * REST endpoints for the player resource: search, lookup, creation, and refresh of persisted
 * player records, plus a freshness check against the Wargaming/WoT API.
 *
 * <p>Exceptions thrown from these endpoints are translated to a consistent error payload by
 * {@code GlobalExceptionHandler}.
 */
@RestController
@RequestMapping("/api/players")
@Validated
public class PlayerController {

    private final PlayerService playerService;

    public PlayerController(PlayerService playerService) {
        this.playerService = playerService;
    }

    /**
     * Indicates, per account, whether the persisted player record is older than the upstream
     * WoT record.
     *
     * @param accountIds the account ids to check
     * @return a map of account id to {@code true} when the local record is stale
     * @throws feign.FeignException if the upstream WoT API call fails
     */
    @GetMapping("/haveUpdated")
    public ResponseEntity<Map<Integer, Boolean>> havePlayersBeenUpdated(
            @RequestParam("accountIds") Integer[] accountIds
    ) {
        return ResponseEntity.ok(playerService.havePlayersBeenUpdated(accountIds));
    }

    /**
     * Returns the persisted player records for the given account ids.
     *
     * @param accountIds the account ids to look up
     * @return a map of account id to {@link PlayerDetails}; missing ids map to {@code null}
     */
    @GetMapping
    public ResponseEntity<Map<Integer, PlayerDetails>> getPlayersMapByAccountIds(
            @RequestParam(value = "accountIds") Integer[] accountIds
    ) {
        return ResponseEntity.ok(playerService.getPlayersMapByAccountIds(accountIds));
    }

    /**
     * Fetches the given accounts from the WoT API and persists any that are not yet stored.
     *
     * @param accountIds the account ids to create
     * @return {@code 201 Created} with a map of account id to the persisted {@link PlayerDetails}
     * @throws feign.FeignException if the upstream WoT API call fails
     */
    @PostMapping
    public ResponseEntity<Map<Integer, PlayerDetails>> createPlayersByAccountIds(
            @RequestParam(value = "accountIds") Integer[] accountIds
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(playerService.createPlayersByAccountIds(accountIds));
    }

    /**
     * Refreshes the persisted player records for the given accounts from the WoT API.
     *
     * @param accountIds the account ids to update
     * @return a map of account id to the refreshed {@link PlayerDetails}
     * @throws feign.FeignException if the upstream WoT API call fails
     */
    @PutMapping
    public ResponseEntity<Map<Integer, PlayerDetails>> updatePlayersByAccountIds(
            @RequestParam(value = "accountIds") Integer[] accountIds
    ) {
        return ResponseEntity.ok(playerService.updatePlayersByAccountId(accountIds));
    }

    /**
     * Searches the WoT API for players matching one or more nicknames.
     *
     * @param nicknames  the nicknames to search for
     * @param language   the WoT API language hint (defaults to {@code en})
     * @param limit      optional cap on results, up to 100
     * @param searchType the WoT search mode (e.g. {@code exact}, {@code startswith})
     * @return matching {@link WotPlayer} records as returned by the WoT API
     * @throws javax.validation.ConstraintViolationException if a parameter fails validation
     * @throws feign.FeignException if the upstream WoT API call fails
     */
    @GetMapping("/list")
    @PlayerSearch
    public ResponseEntity<List<WotPlayer>> getPlayersByNickname(
            @RequestParam(value = "nicknames") String[] nicknames,
            @RequestParam(value = "language", required = false, defaultValue = "en") @Language String language,
            @RequestParam(value = "limit", required = false) @Max(100) Integer limit,
            @RequestParam(value = "searchType") String searchType
    ) {
        return ResponseEntity.ok(playerService.getPlayersByNickname(nicknames, language, limit, searchType));
    }
}
