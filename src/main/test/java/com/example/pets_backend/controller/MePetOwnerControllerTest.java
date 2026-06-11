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

import com.example.pets_backend.dto.resp.PetOwnerRespDTO;
import com.example.pets_backend.frameworks.web.GlobalExceptionHandler;
import com.example.pets_backend.service.PetOwnerService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
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
class MePetOwnerControllerTest {

    private MockMvc mockMvc;

    @Mock
    private PetOwnerService petOwnerService;

    @BeforeEach
    void setUp() {
        ObjectMapper objectMapper = new ObjectMapper()
                .registerModule(new JavaTimeModule())
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        mockMvc = MockMvcBuilders
                .standaloneSetup(new MePetOwnerController(petOwnerService))
                .setControllerAdvice(new GlobalExceptionHandler())
                .setMessageConverters(new MappingJackson2HttpMessageConverter(objectMapper))
                .build();
    }

    @Test
    void createReturnsUnifiedResponse() throws Exception {
        when(petOwnerService.create(any())).thenReturn(new PetOwnerRespDTO(1001L, "13800000002"));

        mockMvc.perform(post("/api/v1/me/pet-owner")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "emergencyContact": "13800000002"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code", is("0")))
                .andExpect(jsonPath("$.data.ownerId", is(1001)))
                .andExpect(jsonPath("$.data.emergencyContact", is("13800000002")));
    }

    @Test
    void detailReturnsPetOwner() throws Exception {
        when(petOwnerService.detail()).thenReturn(new PetOwnerRespDTO(1001L, "13800000002"));

        mockMvc.perform(get("/api/v1/me/pet-owner"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code", is("0")))
                .andExpect(jsonPath("$.data.ownerId", is(1001)));
    }

    @Test
    void updateReturnsUpdatedPetOwner() throws Exception {
        when(petOwnerService.update(any())).thenReturn(new PetOwnerRespDTO(1001L, "13900000003"));

        mockMvc.perform(put("/api/v1/me/pet-owner")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "emergencyContact": "13900000003"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code", is("0")))
                .andExpect(jsonPath("$.data.emergencyContact", is("13900000003")));
    }

    @Test
    void deleteReturnsSuccess() throws Exception {
        mockMvc.perform(delete("/api/v1/me/pet-owner"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code", is("0")))
                .andExpect(jsonPath("$.message", is("success")));
    }
}

