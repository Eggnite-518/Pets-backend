package com.example.pets_backend.controller;

import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.pets_backend.controller.admin.AdminOrderController;
import com.example.pets_backend.dao.UserDao;
import com.example.pets_backend.dao.entity.UserDO;
import com.example.pets_backend.dto.resp.EvidenceChainRespDTO;
import com.example.pets_backend.dto.resp.OrderMetaRespDTO;
import com.example.pets_backend.frameworks.auth.UserContext;
import com.example.pets_backend.frameworks.auth.UserInfoDTO;
import com.example.pets_backend.frameworks.web.GlobalExceptionHandler;
import com.example.pets_backend.service.EvidenceChainService;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

@ExtendWith(MockitoExtension.class)
class AdminOrderControllerTest {

    private MockMvc mockMvc;

    @Mock
    private EvidenceChainService evidenceChainService;

    @Mock
    private UserDao userDao;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(new AdminOrderController(evidenceChainService, userDao))
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
        UserContext.setUser(new UserInfoDTO(9001L, "13900000000", "admin", 3, "token"));
    }

    @AfterEach
    void tearDown() {
        UserContext.clear();
    }

    @Test
    void getEvidenceReturnsUnifiedResponse() throws Exception {
        UserDO admin = new UserDO();
        admin.setUserId(9001L);
        admin.setRoleType(3);
        when(userDao.selectById(9001L)).thenReturn(admin);
        when(evidenceChainService.getEvidenceChain(2002L)).thenReturn(new EvidenceChainRespDTO(
                new OrderMetaRespDTO(2002L, 1001L, 1002L, 6), List.of(), List.of()));

        mockMvc.perform(get("/api/v1/admin/orders/2002/evidence")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code", is("0")))
                .andExpect(jsonPath("$.data.order.orderId", is(2002)))
                .andExpect(jsonPath("$.data.order.orderStatus", is(6)));
    }
}
