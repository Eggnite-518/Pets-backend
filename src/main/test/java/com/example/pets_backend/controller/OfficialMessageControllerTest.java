package com.example.pets_backend.controller;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.pets_backend.dto.resp.OfficialMessageRespDTO;
import com.example.pets_backend.frameworks.auth.UserContext;
import com.example.pets_backend.frameworks.auth.UserInfoDTO;
import com.example.pets_backend.frameworks.web.GlobalExceptionHandler;
import com.example.pets_backend.service.official.OfficialMessageService;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

@ExtendWith(MockitoExtension.class)
class OfficialMessageControllerTest {

    private MockMvc mockMvc;

    @Mock
    private OfficialMessageService officialMessageService;

    @BeforeEach
    void setUp() {
        ObjectMapper objectMapper = new ObjectMapper();
        mockMvc = MockMvcBuilders
                .standaloneSetup(new OfficialMessageController(officialMessageService))
                .setControllerAdvice(new GlobalExceptionHandler())
                .setMessageConverters(new MappingJackson2HttpMessageConverter(objectMapper))
                .build();
    }

    @AfterEach
    void tearDown() {
        UserContext.clear();
    }

    @Test
    void listOfficialMessagesUsesCurrentUser() throws Exception {
        UserContext.setUser(new UserInfoDTO(1001L, "13800000001", "宠主", 1, "jwt-token"));
        when(officialMessageService.listOfficialMessages(eq(2002L), eq(1001L)))
                .thenReturn(List.of(new OfficialMessageRespDTO(9001L, 2002L, 999999L, 1001L,
                        "【系统通知】订单2002可能迟到", "2026-05-23 10:00:00")));

        mockMvc.perform(get("/api/v1/messages/official").param("orderId", "2002"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code", is("0")))
                .andExpect(jsonPath("$.data[0].messageId", is(9001)))
                .andExpect(jsonPath("$.data[0].senderId", is(999999)))
                .andExpect(jsonPath("$.data[0].receiverId", is(1001)));

        verify(officialMessageService).listOfficialMessages(2002L, 1001L);
    }
}

