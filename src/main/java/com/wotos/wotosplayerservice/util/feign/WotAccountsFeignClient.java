package com.wotos.wotosplayerservice.util.feign;

import com.wotos.wotosplayerservice.config.FeignConfig;
import com.wotos.wotosplayerservice.util.model.wot.WotApiResponse;
import com.wotos.wotosplayerservice.util.model.wot.achievements.WotAchievements;
import com.wotos.wotosplayerservice.util.model.wot.player.WotPlayer;
import com.wotos.wotosplayerservice.util.model.wot.player.WotPlayerDetails;
import com.wotos.wotosplayerservice.util.model.wot.player.WotPlayerVehicle;
import com.wotos.wotosplayerservice.validation.constraints.Language;
import com.wotos.wotosplayerservice.validation.constraints.PlayerSearch;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Map;

@FeignClient(name = "WotAccountsFeignClient", url = "${env.urls.world_of_tanks_api}", configuration = FeignConfig.class)
public interface WotAccountsFeignClient {

    @GetMapping(value = "/account/list/", consumes = "application/json")
    @PlayerSearch
    ResponseEntity<WotApiResponse<List<WotPlayer>>> getPlayersByExactNickname(
            @RequestParam(value = "application_id") String appId,
            @RequestParam(value = "search") String[] nicknames,
            @RequestParam(value = "language", required = false, defaultValue = "en") @Language String language,
            @RequestParam(value = "limit", required = false, defaultValue = "100") Integer limit,
            @RequestParam(value = "type", required = false, defaultValue = "exact") String searchType
    );

    @GetMapping(value = "/account/info/", consumes = "application/json")
    ResponseEntity<WotApiResponse<Map<Integer, WotPlayerDetails>>> getPlayerDetails(
            @RequestParam(value = "application_id") String appId,
            @RequestParam(value = "access_token", required = false) String accessToken,
            @RequestParam(value = "extra", required = false) String[] extras,
            @RequestParam(value = "fields", required = false) String[] fields,
            @RequestParam(value = "language", required = false, defaultValue = "en") @Language String language,
            @RequestParam(value = "account_id") Integer[] accountIds
    );

    @GetMapping(value = "/account/tanks/")
    ResponseEntity<WotApiResponse<List<WotPlayerVehicle>>> getPlayerVehicles(
            @RequestParam(value = "application_id") String appId,
            @RequestParam(value = "account_id") Integer[] accountIds,
            @RequestParam(value = "access_token", required = false) String accessToken,
            @RequestParam(value = "fields", required = false) String[] fields,
            @RequestParam(value = "language", required = false, defaultValue = "en") @Language String language,
            @RequestParam(value = "tank_id", required = false) Integer[] vehicleIds
    );

    @GetMapping(value = "/account/achievements/")
    ResponseEntity<WotApiResponse<WotAchievements>> getPlayerAchievements(
            @RequestParam(value = "application_id") String appId,
            @RequestParam(value = "account_id") Integer[] accountIds,
            @RequestParam(value = "fields", required = false) String[] fields,
            @RequestParam(value = "language", required = false, defaultValue = "en") @Language String language
    );

}
