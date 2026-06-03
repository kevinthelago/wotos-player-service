package com.wotos.wotosplayerservice.controller;

import com.wotos.wotosplayerservice.exception.EntityNotFoundException;
import com.wotos.wotosplayerservice.exception.GlobalExceptionHandler;
import com.wotos.wotosplayerservice.service.PlayerAchievementsService;
import com.wotos.wotosplayerservice.util.model.PlayerAchievementsResponse;
import feign.FeignException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

import java.util.HashMap;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * {@link WebMvcTest} slice for {@link PlayerAchievementsController}: endpoint routes and
 * {@link GlobalExceptionHandler} integration with the service mocked.
 */
@WebMvcTest(PlayerAchievementsController.class)
@Import(GlobalExceptionHandler.class)
class PlayerAchievementsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PlayerAchievementsService playerAchievementsService;

    @Test
    void getPlayerAchievementsReturns200() throws Exception {
        when(playerAchievementsService.getPlayerAchievements(any()))
                .thenReturn(new PlayerAchievementsResponse(1, null, new HashMap<>()));

        mockMvc.perform(get("/api/players/1/achievements"))
                .andExpect(status().isOk());
    }

    @Test
    void getPlayerAchievementsForUnknownAccountReturns404() throws Exception {
        when(playerAchievementsService.getPlayerAchievements(any()))
                .thenThrow(new EntityNotFoundException("No achievements found for account 1"));

        mockMvc.perform(get("/api/players/1/achievements"))
                .andExpect(status().isNotFound());
    }

    @Test
    void getPlayerAchievementsByAccountIdsReturns200() throws Exception {
        when(playerAchievementsService.getPlayerAchievementsSnapshotsByAccountIds(any()))
                .thenReturn(new HashMap<>());

        mockMvc.perform(get("/api/players/achievements").param("accountIds", "1"))
                .andExpect(status().isOk());
    }

    @Test
    void createPlayerAchievementsByAccountIdsReturns200() throws Exception {
        when(playerAchievementsService.createPlayerAchievementsSnapshotsByAccountIds(any()))
                .thenReturn(new HashMap<>());

        mockMvc.perform(post("/api/players/achievements").param("accountIds", "1"))
                .andExpect(status().isOk());
    }

    @Test
    void feignExceptionFromServiceMapsTo502() throws Exception {
        FeignException feignEx = mock(FeignException.class);
        when(playerAchievementsService.createPlayerAchievementsSnapshotsByAccountIds(any()))
                .thenThrow(feignEx);

        mockMvc.perform(post("/api/players/achievements").param("accountIds", "1"))
                .andExpect(status().isBadGateway());
    }
}
