package com.wotos.wotosplayerservice.controller;

import com.wotos.wotosplayerservice.exception.GlobalExceptionHandler;
import com.wotos.wotosplayerservice.service.PlayerSnapshotService;
import feign.FeignException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.HashMap;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * MockMvc tests covering {@link PlayerSnapshotController}'s endpoint routes and the
 * integration with {@link GlobalExceptionHandler}.
 */
@RunWith(MockitoJUnitRunner.class)
public class PlayerSnapshotControllerTest {

    @Mock
    private PlayerSnapshotService playerSnapshotService;

    @InjectMocks
    private PlayerSnapshotController playerSnapshotController;

    private MockMvc mockMvc;

    @Before
    public void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(playerSnapshotController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    public void getPlayerSnapshotsByAccountIdsReturns200() throws Exception {
        when(playerSnapshotService.getPlayerSnapshotsByAccountIds(any())).thenReturn(new HashMap<>());

        mockMvc.perform(get("/api/player/snapshots").param("accountIds", "1"))
                .andExpect(status().isOk());
    }

    @Test
    public void createPlayerSnapshotsByAccountIdsReturns200() throws Exception {
        when(playerSnapshotService.createPlayerSnapshotByAccountIds(any())).thenReturn(new HashMap<>());

        mockMvc.perform(post("/api/player/snapshots").param("accountIds", "1"))
                .andExpect(status().isOk());
    }

    @Test
    public void feignExceptionFromServiceMapsTo502() throws Exception {
        FeignException feignEx = mock(FeignException.class);
        when(playerSnapshotService.createPlayerSnapshotByAccountIds(any())).thenThrow(feignEx);

        mockMvc.perform(post("/api/player/snapshots").param("accountIds", "1"))
                .andExpect(status().isBadGateway());
    }
}
