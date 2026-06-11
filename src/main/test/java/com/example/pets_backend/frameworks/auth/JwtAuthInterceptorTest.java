package com.example.pets_backend.frameworks.auth;

import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.pets_backend.frameworks.convention.errorcode.BaseErrorCode;
import com.example.pets_backend.frameworks.convention.result.Result;
import com.example.pets_backend.frameworks.web.GlobalExceptionHandler;
import com.example.pets_backend.frameworks.web.Results;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

class JwtAuthInterceptorTest {

    private MockMvc mockMvc;
    private JwtUtil jwtUtil;

    @BeforeEach
    void setUp() {
        JwtProperties jwtProperties = new JwtProperties();
        jwtProperties.setSecret("pets-backend-default-secret-key-change-me-1234567890");
        jwtProperties.setIssuer("pets-backend");
        jwtProperties.setTtlSeconds(3600);
        jwtUtil = new JwtUtil(jwtProperties);
        mockMvc = MockMvcBuilders
                .standaloneSetup(new TestAuthController())
                .addInterceptors(new JwtAuthInterceptor(jwtUtil))
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void requestWithoutTokenReturnsUnifiedError() throws Exception {
        mockMvc.perform(get("/api/v1/me")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code", is(BaseErrorCode.AUTH_TOKEN_MISSING_ERROR.code())))
                .andExpect(jsonPath("$.message", is(BaseErrorCode.AUTH_TOKEN_MISSING_ERROR.message())));
    }

    @Test
    void requestWithBearerTokenPopulatesUserContext() throws Exception {
        String token = jwtUtil.generateAccessToken(new UserInfoDTO(1001L, "13800000000", "宠物主人", 1, null));

        mockMvc.perform(get("/api/v1/me")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code", is("0")))
                .andExpect(jsonPath("$.data.userId", is(1001)))
                .andExpect(jsonPath("$.data.nickname", is("宠物主人")))
                .andExpect(jsonPath("$.data.phone", is("13800000000")));

        assertNull(UserContext.getUser());
    }

    @RestController
    static class TestAuthController {

        @GetMapping("/api/v1/me")
        public Result<UserInfoDTO> me() {
            return Results.success(UserContext.getUser());
        }
    }
}
