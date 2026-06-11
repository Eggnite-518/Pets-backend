package com.example.pets_backend.controller;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.pets_backend.dto.resp.UserAddressRespDTO;
import com.example.pets_backend.frameworks.web.GlobalExceptionHandler;
import com.example.pets_backend.service.UserAddressService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
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
class UserAddressControllerTest {

    private MockMvc mockMvc;

    @Mock
    private UserAddressService userAddressService;

    @BeforeEach
    void setUp() {
        ObjectMapper objectMapper = new ObjectMapper()
                .registerModule(new JavaTimeModule())
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        mockMvc = MockMvcBuilders
                .standaloneSetup(new UserAddressController(userAddressService))
                .setControllerAdvice(new GlobalExceptionHandler())
                .setMessageConverters(new MappingJackson2HttpMessageConverter(objectMapper))
                .build();
    }

    @Test
    void createReturnsUnifiedResponse() throws Exception {
        when(userAddressService.create(any())).thenReturn(new UserAddressRespDTO(
                5001L,
                1001L,
                "张三",
                "13800000002",
                "广东省",
                "深圳市",
                "南山区",
                "科技园1号",
                "家庭",
                1,
                39.9042000,
                116.4074000));

        mockMvc.perform(post("/api/v1/user-addresses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "contactName": "张三",
                                  "contactPhone": "13800000002",
                                  "province": "广东省",
                                  "city": "深圳市",
                                  "district": "南山区",
                                  "detailAddress": "科技园1号",
                                  "addressTag": "家庭",
                                   "isDefault": 1,
                                   "latitude": 39.9042,
                                   "longitude": 116.4074
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code", is("0")))
                .andExpect(jsonPath("$.data.addressId", is(5001)))
                .andExpect(jsonPath("$.data.userId", is(1001)))
                .andExpect(jsonPath("$.data.contactName", is("张三")))
                .andExpect(jsonPath("$.data.latitude", is(39.9042)))
                .andExpect(jsonPath("$.data.longitude", is(116.4074)));
    }

    @Test
    void detailReturnsAddress() throws Exception {
        when(userAddressService.detail(5001L)).thenReturn(new UserAddressRespDTO(
                5001L,
                1001L,
                "张三",
                "13800000002",
                "广东省",
                "深圳市",
                "南山区",
                "科技园1号",
                "家庭",
                1,
                39.9042000,
                116.4074000));

        mockMvc.perform(get("/api/v1/user-addresses/5001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code", is("0")))
                .andExpect(jsonPath("$.data.addressId", is(5001)))
                .andExpect(jsonPath("$.data.contactPhone", is("13800000002")));
    }

    @Test
    void listReturnsAddresses() throws Exception {
        when(userAddressService.list()).thenReturn(List.of(
                new UserAddressRespDTO(5001L, 1001L, "张三", "13800000002", "广东省", "深圳市", "南山区", "科技园1号", "家庭", 1, 39.9042000, 116.4074000),
                new UserAddressRespDTO(5002L, 1001L, "李四", "13800000003", "广东省", "深圳市", "福田区", "购物公园", null, 0, null, null)));

        mockMvc.perform(get("/api/v1/user-addresses"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code", is("0")))
                .andExpect(jsonPath("$.data[0].contactName", is("张三")))
                .andExpect(jsonPath("$.data[1].contactName", is("李四")));
    }

    @Test
    void updateReturnsAddress() throws Exception {
        when(userAddressService.update(any(), any())).thenReturn(new UserAddressRespDTO(
                5001L,
                1001L,
                "张三",
                "13800000002",
                "广东省",
                "深圳市",
                "南山区",
                "科技园1号",
                "家庭",
                1,
                39.9042000,
                116.4074000));

        mockMvc.perform(put("/api/v1/user-addresses/5001")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "contactName": "张三",
                                  "contactPhone": "13800000002",
                                  "province": "广东省",
                                  "city": "深圳市",
                                  "district": "南山区",
                                  "detailAddress": "科技园1号",
                                  "addressTag": "家庭",
                                   "isDefault": 1,
                                   "latitude": 39.9042,
                                   "longitude": 116.4074
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code", is("0")))
                .andExpect(jsonPath("$.data.addressId", is(5001)))
                .andExpect(jsonPath("$.data.latitude", is(39.9042)))
                .andExpect(jsonPath("$.data.longitude", is(116.4074)));
    }

    @Test
    void switchDefaultReturnsAddress() throws Exception {
        when(userAddressService.switchDefault(5001L)).thenReturn(new UserAddressRespDTO(
                5001L,
                1001L,
                "张三",
                "13800000002",
                "广东省",
                "深圳市",
                "南山区",
                "科技园1号",
                "家庭",
                1,
                39.9042000,
                116.4074000));

        mockMvc.perform(put("/api/v1/user-addresses/5001/default"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code", is("0")))
                .andExpect(jsonPath("$.data.addressId", is(5001)))
                .andExpect(jsonPath("$.data.isDefault", is(1)));
    }

    @Test
    void deleteReturnsSuccess() throws Exception {
        mockMvc.perform(delete("/api/v1/user-addresses/5001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code", is("0")))
                .andExpect(jsonPath("$.message", is("success")));
    }
}


