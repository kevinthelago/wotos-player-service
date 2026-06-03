package com.wotos.wotosplayerservice.util.feign;

import com.wotos.wotosplayerservice.config.FeignConfig;
import com.wotos.wotosplayerservice.util.model.wot.WotApiResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

@FeignClient(name = "WoTPlayerVehiclesFeignClient", url = "${env.urls.world_of_tanks_api}", configuration = FeignConfig.class)
public interface WotPlayerVehiclesFeignClient {

    @GetMapping("/tanks/achievements/")
    ResponseEntity<WotApiResponse<String>> getPlayerVehicleAchievements(
            @RequestParam("application_id") String appId,
            @RequestParam("account_id") Integer[] accountIds,
            @RequestParam("access_token") String accessToken,
            @RequestParam("fields") String[] fields,
            @RequestParam("in_garage") @Max(1) @Min(0) Integer inGarage,
            @RequestParam("language") String language,
            @RequestParam("tank_id") Integer[] vehicleIds
    );

}
