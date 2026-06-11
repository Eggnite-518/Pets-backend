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

import com.example.pets_backend.dto.resp.PetArchiveRespDTO;
import com.example.pets_backend.frameworks.web.GlobalExceptionHandler;
import com.example.pets_backend.service.PetArchiveService;
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
class PetArchiveControllerTest {

    private MockMvc mockMvc;

    @Mock
    private PetArchiveService petArchiveService;

    @BeforeEach
    void setUp() {
        ObjectMapper objectMapper = new ObjectMapper()
                .registerModule(new JavaTimeModule())
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        mockMvc = MockMvcBuilders
                .standaloneSetup(new PetArchiveController(petArchiveService))
                .setControllerAdvice(new GlobalExceptionHandler())
                .setMessageConverters(new MappingJackson2HttpMessageConverter(objectMapper))
                .build();
    }

    @Test
    void createReturnsUnifiedResponse() throws Exception {
        when(petArchiveService.create(any())).thenReturn(buildPetArchive(3001L, "团团", 1, "猫"));

        mockMvc.perform(post("/api/v1/pet-archives")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "petName": "团团",
                                  "petType": 1,
                                  "defaultReq": "每日更换清水",
                                  "image": "https://example.com/pets/tuantuan.jpg"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code", is("0")))
                .andExpect(jsonPath("$.data.petId", is(3001)))
                .andExpect(jsonPath("$.data.ownerId", is(1001)))
                .andExpect(jsonPath("$.data.petName", is("团团")))
                .andExpect(jsonPath("$.data.petTypeDesc", is("猫")))
                .andExpect(jsonPath("$.data.image", is("https://example.com/pets/tuantuan.jpg")));
    }

    @Test
    void detailReturnsPetArchive() throws Exception {
        when(petArchiveService.detail(3001L)).thenReturn(buildPetArchive(3001L, "团团", 1, "猫"));

        mockMvc.perform(get("/api/v1/pet-archives/3001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code", is("0")))
                .andExpect(jsonPath("$.data.petId", is(3001)))
                .andExpect(jsonPath("$.data.petName", is("团团")))
                .andExpect(jsonPath("$.data.petTypeDesc", is("猫")));
    }

    @Test
    void listReturnsPetArchives() throws Exception {
        when(petArchiveService.list(any(), any())).thenReturn(List.of(
                buildPetArchive(3001L, "团团", 1, "猫"),
                buildPetArchive(3002L, "可乐", 2, "狗")));

        mockMvc.perform(get("/api/v1/pet-archives")
                        .param("petName", "团")
                        .param("petType", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code", is("0")))
                .andExpect(jsonPath("$.data[0].petName", is("团团")))
                .andExpect(jsonPath("$.data[0].image", is("https://example.com/pets/tuantuan.jpg")))
                .andExpect(jsonPath("$.data[1].petName", is("可乐")));
    }

    @Test
    void updateReturnsUpdatedPetArchive() throws Exception {
        when(petArchiveService.update(any(), any())).thenReturn(buildPetArchive(3001L, "团团", 1, "猫"));

        mockMvc.perform(put("/api/v1/pet-archives/3001")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "petName": "团团",
                                  "petType": 1,
                                  "defaultReq": "每日更换清水",
                                  "image": "https://example.com/pets/tuantuan.jpg"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code", is("0")))
                .andExpect(jsonPath("$.data.petId", is(3001)))
                .andExpect(jsonPath("$.data.defaultReq", is("每日更换清水")));
    }

    @Test
    void deleteReturnsSuccess() throws Exception {
        mockMvc.perform(delete("/api/v1/pet-archives/3001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code", is("0")))
                .andExpect(jsonPath("$.message", is("success")));
    }

    private PetArchiveRespDTO buildPetArchive(Long petId, String petName, Integer petType, String petTypeDesc) {
        String image = petId == 3001L
                ? "https://example.com/pets/tuantuan.jpg"
                : "https://example.com/pets/kele.jpg";
        return new PetArchiveRespDTO(
                petId,
                1001L,
                petName,
                petType,
                petTypeDesc,
                "每日更换清水",
                image,
                null);
    }
}
