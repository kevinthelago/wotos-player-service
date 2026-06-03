package com.wotos.wotosplayerservice.controller;

import com.wotos.wotosplayerservice.exception.EntityNotFoundException;
import com.wotos.wotosplayerservice.exception.GlobalExceptionHandler;
import com.wotos.wotosplayerservice.service.PlayerService;
import com.wotos.wotosplayerservice.util.model.wot.player.WotPlayer;
import feign.FeignException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;
import java.util.HashMap;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * {@link WebMvcTest} slice for {@link PlayerController}: verifies endpoint routes, status codes,
 * and the {@link GlobalExceptionHandler} error-response shape with the service mocked.
 */
@WebMvcTest(PlayerController.class)
@Import(GlobalExceptionHandler.class)
class PlayerControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PlayerService playerService;

    @Test
    void getPlayersMapByAccountIdsReturns200() throws Exception {
        when(playerService.getPlayersMapByAccountIds(any())).thenReturn(new HashMap<>());

        mockMvc.perform(get("/api/players").param("accountIds", "1", "2"))
                .andExpect(status().isOk());
    }

    @Test
    void createPlayersByAccountIdsReturns201() throws Exception {
        when(playerService.createPlayersByAccountIds(any())).thenReturn(new HashMap<>());

        mockMvc.perform(post("/api/players").param("accountIds", "1"))
                .andExpect(status().isCreated());
    }

    @Test
    void updatePlayersByAccountIdsReturns200() throws Exception {
        when(playerService.updatePlayersByAccountId(any())).thenReturn(new HashMap<>());

        mockMvc.perform(put("/api/players").param("accountIds", "1"))
                .andExpect(status().isOk());
    }

    @Test
    void havePlayersBeenUpdatedReturns200() throws Exception {
        when(playerService.havePlayersBeenUpdated(any())).thenReturn(new HashMap<>());

        mockMvc.perform(get("/api/players/haveUpdated").param("accountIds", "1"))
                .andExpect(status().isOk());
    }

    @Test
    void getPlayersByNicknameReturns200() throws Exception {
        when(playerService.getPlayersByNickname(any(), anyString(), any(), anyString()))
                .thenReturn(Collections.<WotPlayer>emptyList());

        mockMvc.perform(get("/api/players/list")
                        .param("nicknames", "foo")
                        .param("searchType", "exact"))
                .andExpect(status().isOk());
    }

    @Test
    void getPlayersByNicknameRejectsLimitOver100() throws Exception {
        mockMvc.perform(get("/api/players/list")
                        .param("nicknames", "foo")
                        .param("searchType", "exact")
                        .param("limit", "500"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value(400));
    }

    @Test
    void entityNotFoundFromServiceMapsTo404() throws Exception {
        when(playerService.getPlayersMapByAccountIds(any()))
                .thenThrow(new EntityNotFoundException("player 1 not found"));

        mockMvc.perform(get("/api/players").param("accountIds", "1"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error.code").value(404))
                .andExpect(jsonPath("$.error.message").value("player 1 not found"))
                .andExpect(jsonPath("$.error.correlationId").isNotEmpty());
    }

    @Test
    void feignExceptionFromServiceMapsTo502() throws Exception {
        FeignException feignEx = mock(FeignException.class);
        when(playerService.createPlayersByAccountIds(any())).thenThrow(feignEx);

        mockMvc.perform(post("/api/players").param("accountIds", "1"))
                .andExpect(status().isBadGateway())
                .andExpect(jsonPath("$.error.code").value(502));
    }
}
