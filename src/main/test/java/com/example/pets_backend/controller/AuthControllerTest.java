package com.example.pets_backend.controller;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.pets_backend.dto.req.RegisterUserReqDTO;
import com.example.pets_backend.dto.req.LoginUserReqDTO;
import com.example.pets_backend.dto.req.ResetPasswordReqDTO;
import com.example.pets_backend.dto.resp.LoginUserRespDTO;
import com.example.pets_backend.dto.resp.RegisterUserRespDTO;
import com.example.pets_backend.frameworks.web.GlobalExceptionHandler;
import com.example.pets_backend.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

        private MockMvc mockMvc;

        @Mock
        private UserService userService;

        @BeforeEach
        void setUp() {
                ObjectMapper objectMapper = new ObjectMapper()
                                .registerModule(new JavaTimeModule())
                                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
                mockMvc = MockMvcBuilders
                                .standaloneSetup(new AuthController(userService))
                                .setControllerAdvice(new GlobalExceptionHandler())
                                .setMessageConverters(new MappingJackson2HttpMessageConverter(objectMapper))
                                .build();
        }

        @Test
        void registerReturnsUnifiedResponse() throws Exception {
                when(userService.register(any(RegisterUserReqDTO.class)))
                                .thenReturn(new RegisterUserRespDTO(
                                                "\u7528\u62370000",
                                                "13800000000"));

                mockMvc.perform(post("/api/v1/auth/register")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                                {
                                                  "nickname": "\u7528\u62370000",
                                                  "phone": "13800000000",
                                                  "password": "Abc12345!"
                                                }
                                                """))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.code", is("0")))
                                .andExpect(jsonPath("$.message", is("success")))
                                .andExpect(jsonPath("$.data.phone", is("13800000000")))
                                .andExpect(jsonPath("$.data.nickname", is("\u7528\u62370000")));
        }

        @Test
        void resetPasswordReturnsSuccess() throws Exception {
                mockMvc.perform(post("/api/v1/auth/reset-password")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                                {
                                                  "phone": "13800000000",
                                                  "code": "123456",
                                                  "newPassword": "Abc12345!"
                                                }
                                                """))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.code", is("0")))
                                .andExpect(jsonPath("$.message", is("success")));
        }

        @Test
        void loginReturnsJwtToken() throws Exception {
                when(userService.login(any(LoginUserReqDTO.class)))
                                .thenReturn(new LoginUserRespDTO(
                                                1L,
                                                "\u7528\u6237A",
                                                "13800000000",
                                                1,
                                                "仅宠主",
                                                "jwt-token"));

                mockMvc.perform(post("/api/v1/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                                {
                                                  "phone": "13800000000",
                                                  "password": "Abc12345!"
                                                }
                                                """))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.code", is("0")))
                                .andExpect(jsonPath("$.message", is("success")))
                                .andExpect(jsonPath("$.data.userId", is(1)))
                                .andExpect(jsonPath("$.data.phone", is("13800000000")))
                                .andExpect(jsonPath("$.data.token", is("jwt-token")));
        }
}
