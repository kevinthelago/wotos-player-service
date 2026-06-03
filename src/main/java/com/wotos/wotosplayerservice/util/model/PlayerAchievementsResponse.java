package com.wotos.wotosplayerservice.util.model;

import com.wotos.wotosplayerservice.util.model.wot.achievements.WotPlayerAchievements;

import java.util.Map;

/**
 * Response for {@code GET /api/players/{accountId}/achievements}: the player's earned
 * achievements paired with the (cached) achievement metadata catalogue needed to render them.
 *
 * @param accountId    the player's account id
 * @param achievements the player's earned achievements as returned by the WoT API
 * @param metadata     the achievement encyclopedia, keyed by achievement id
 */
public record PlayerAchievementsResponse(
        Integer accountId,
        WotPlayerAchievements achievements,
        Map<String, Object> metadata
) {
}
