package com.wotos.wotosplayerservice.controller;

import com.wotos.wotosplayerservice.exception.GlobalExceptionHandler;
import com.wotos.wotosplayerservice.service.PlayerSnapshotService;
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
 * {@link WebMvcTest} slice for {@link PlayerSnapshotController}: endpoint routes and
 * {@link GlobalExceptionHandler} integration with the service mocked.
 */
@WebMvcTest(PlayerSnapshotController.class)
@Import(GlobalExceptionHandler.class)
class PlayerSnapshotControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PlayerSnapshotService playerSnapshotService;

    @Test
    void getPlayerSnapshotsByAccountIdsReturns200() throws Exception {
        when(playerSnapshotService.getPlayerSnapshotsByAccountIds(any(), any(), any()))
                .thenReturn(new HashMap<>());

        mockMvc.perform(get("/api/players/snapshots").param("accountIds", "1"))
                .andExpect(status().isOk());
    }

    @Test
    void createPlayerSnapshotsByAccountIdsReturns200() throws Exception {
        when(playerSnapshotService.createPlayerSnapshotByAccountIds(any())).thenReturn(new HashMap<>());

        mockMvc.perform(post("/api/players/snapshots").param("accountIds", "1"))
                .andExpect(status().isOk());
    }

    @Test
    void feignExceptionFromServiceMapsTo502() throws Exception {
        FeignException feignEx = mock(FeignException.class);
        when(playerSnapshotService.createPlayerSnapshotByAccountIds(any())).thenThrow(feignEx);

        mockMvc.perform(post("/api/players/snapshots").param("accountIds", "1"))
                .andExpect(status().isBadGateway());
    }
}
