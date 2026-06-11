package com.example.pets_backend.controller;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.pets_backend.dto.resp.CaretakerWalletBalanceRespDTO;
import com.example.pets_backend.dto.resp.CaretakerWalletRecordRespDTO;
import com.example.pets_backend.dto.resp.CaretakerWalletRecordsRespDTO;
import com.example.pets_backend.dto.resp.CaretakerWalletWithdrawRespDTO;
import com.example.pets_backend.frameworks.web.GlobalExceptionHandler;
import com.example.pets_backend.service.WalletService;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

@ExtendWith(MockitoExtension.class)
class CaretakerMeWalletControllerTest {

    private MockMvc mockMvc;

    @Mock
    private WalletService walletService;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(new CaretakerMeWalletController(walletService))
                .setControllerAdvice(new GlobalExceptionHandler())
                .setMessageConverters(new MappingJackson2HttpMessageConverter(new ObjectMapper()))
                .build();
    }

    @Test
    void getWalletBalanceReturnsBalance() throws Exception {
        when(walletService.getCaretakerBalance()).thenReturn(new CaretakerWalletBalanceRespDTO("2480.50"));

        mockMvc.perform(get("/api/v1/caretaker/me/wallet"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code", is("0")))
                .andExpect(jsonPath("$.data.balance", is("2480.50")));
    }

    @Test
    void applyWithdrawReturnsBalance() throws Exception {
        when(walletService.applyCaretakerWithdraw(any()))
                .thenReturn(new CaretakerWalletWithdrawRespDTO("2280.50"));

        mockMvc.perform(get("/api/v1/caretaker/me/wallet/withdraw")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"amount\":200}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code", is("0")))
                .andExpect(jsonPath("$.data.balance", is("2280.50")));
    }

    @Test
    void listWalletRecordsReturnsPagedRecords() throws Exception {
        when(walletService.listCaretakerWalletRecords(anyInt(), anyInt()))
                .thenReturn(new CaretakerWalletRecordsRespDTO(
                        1,
                        1,
                        20,
                        List.of(new CaretakerWalletRecordRespDTO(
                                "5001",
                                11,
                                "订单收益",
                                1,
                                "65.00",
                                "订单 #10086 完成",
                                "2026-05-20 14:30:00"))));

        mockMvc.perform(get("/api/v1/caretaker/me/wallet/records?page=1&pageSize=20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code", is("0")))
                .andExpect(jsonPath("$.data.total", is(1)))
                .andExpect(jsonPath("$.data.list[0].recordId", is("5001")));
    }
}
