- smoke-test: WoT Feign paths verified live (/wot/account/list,info,achievements return JSON envelope not 404); WG_APP_ID not in worker env so used placeholder — director/secret store must supply real id for authenticated calls

- CONTRACT (backend-edge): player snapshot+achievement endpoints standardized under /api/players (plural) to match PlayerController + edge WotosPlayerFeignClient. New: GET /api/players/{accountId}/achievements (live). Snapshots: GET/POST /api/players/snapshots. edge currently has achievements commented out + no snapshots calls, so no break.

- CONTRACT/director: player error envelope per its issue-5 kickoff is {error:{code,message,correlationId}} -- DIVERGES from statistics-service {status,message,timestamp}. Following player kickoff (authoritative). Flag for mesh-wide consistency decision if desired.

- OBSERVED: edge WotosVehicleFeignClient targets name=wotos-player-service for /api/vehicles -- likely should be wotos-vehicle-service. edge's bug, not acting.

- CONTRACT(backend-edge): NEW GET /api/players/{accountId}/achievements -> PlayerAchievementsResponse{accountId, achievements(WotPlayerAchievements), metadata(Map)}. Metadata cached 1d (achievement-meta). edge's commented-out achievements stub used @PathParam list-return -- update to this shape when wiring.

- CONTRACT(backend-edge): player error body is now {error:{code,message,correlationId}} (was {status,message,timestamp}). Update edge error-handling/translation accordingly. 502=WoT error, 504=WoT timeout, 404=not found, 400=validation, 500=generic.

