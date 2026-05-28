package com.wotos.wotosplayerservice.controller;

import com.wotos.wotosplayerservice.exception.EntityNotFoundException;
import com.wotos.wotosplayerservice.exception.GlobalExceptionHandler;
import com.wotos.wotosplayerservice.service.PlayerService;
import com.wotos.wotosplayerservice.util.model.wot.player.WotPlayer;
import feign.FeignException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

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
 * MockMvc tests covering {@link PlayerController}'s endpoint routes, status codes, and the
 * integration with {@link GlobalExceptionHandler} for error-response shapes.
 */
@RunWith(MockitoJUnitRunner.class)
public class PlayerControllerTest {

    @Mock
    private PlayerService playerService;

    @InjectMocks
    private PlayerController playerController;

    private MockMvc mockMvc;

    @Before
    public void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(playerController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    public void getPlayersMapByAccountIdsReturns200() throws Exception {
        when(playerService.getPlayersMapByAccountIds(any())).thenReturn(new HashMap<>());

        mockMvc.perform(get("/api/players").param("accountIds", "1", "2"))
                .andExpect(status().isOk());
    }

    @Test
    public void createPlayersByAccountIdsReturns201() throws Exception {
        when(playerService.createPlayersByAccountIds(any())).thenReturn(new HashMap<>());

        mockMvc.perform(post("/api/players").param("accountIds", "1"))
                .andExpect(status().isCreated());
    }

    @Test
    public void updatePlayersByAccountIdsReturns200() throws Exception {
        when(playerService.updatePlayersByAccountId(any())).thenReturn(new HashMap<>());

        mockMvc.perform(put("/api/players").param("accountIds", "1"))
                .andExpect(status().isOk());
    }

    @Test
    public void havePlayersBeenUpdatedReturns200() throws Exception {
        when(playerService.havePlayersBeenUpdated(any())).thenReturn(new HashMap<>());

        mockMvc.perform(get("/api/players/haveUpdated").param("accountIds", "1"))
                .andExpect(status().isOk());
    }

    @Test
    public void getPlayersByNicknameReturns200() throws Exception {
        when(playerService.getPlayersByNickname(any(), anyString(), any(), anyString()))
                .thenReturn(Collections.<WotPlayer>emptyList());

        mockMvc.perform(get("/api/players/list")
                        .param("nicknames", "foo")
                        .param("searchType", "exact"))
                .andExpect(status().isOk());
    }

    @Test
    public void entityNotFoundFromServiceMapsTo404() throws Exception {
        when(playerService.getPlayersMapByAccountIds(any()))
                .thenThrow(new EntityNotFoundException("player 1 not found"));

        mockMvc.perform(get("/api/players").param("accountIds", "1"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("player 1 not found"));
    }

    @Test
    public void feignExceptionFromServiceMapsTo502() throws Exception {
        FeignException feignEx = mock(FeignException.class);
        when(playerService.createPlayersByAccountIds(any())).thenThrow(feignEx);

        mockMvc.perform(post("/api/players").param("accountIds", "1"))
                .andExpect(status().isBadGateway())
                .andExpect(jsonPath("$.status").value(502));
    }
}
