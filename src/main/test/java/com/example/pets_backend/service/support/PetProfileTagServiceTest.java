package com.example.pets_backend.service.support;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.example.pets_backend.dto.req.PetProfileTagsReqDTO;
import com.example.pets_backend.dto.resp.PetProfileTagsRespDTO;
import com.example.pets_backend.frameworks.convention.exception.ClientException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class PetProfileTagServiceTest {

    private PetProfileTagService petProfileTagService;

    @BeforeEach
    void setUp() {
        petProfileTagService = new PetProfileTagService(new ObjectMapper());
    }

    @Test
    void normalizeAndSerializeProfileTags() {
        PetProfileTagsReqDTO tags = new PetProfileTagsReqDTO(
                new BigDecimal("4.50"),
                "ADULT",
                List.of("NEUTERED"),
                4,
                "MEDIUM",
                List.of("BOLT_PULL"),
                List.of("DESTRUCTIVE"),
                List.of("MEDICATION", "ALLERGY"));

        String json = petProfileTagService.serialize(tags);
        assertNotNull(json);

        PetProfileTagsReqDTO parsed = petProfileTagService.parse(json);
        assertEquals(new BigDecimal("4.50"), parsed.weightKg());
        assertEquals("ADULT", parsed.ageGroup());
        assertEquals(List.of("NEUTERED"), parsed.physiologicalStates());
        assertEquals(4, parsed.socialFriendliness());
        assertEquals("MEDIUM", parsed.aggressionLevel());
        assertEquals(List.of("BOLT_PULL"), parsed.outdoorBehaviors());
        assertEquals(List.of("DESTRUCTIVE"), parsed.indoorBehaviors());
        assertEquals(List.of("MEDICATION", "ALLERGY"), parsed.healthTags());
    }

    @Test
    void toRespDTOIncludesDescriptions() {
        PetProfileTagsRespDTO resp = petProfileTagService.toRespDTO(new PetProfileTagsReqDTO(
                new BigDecimal("2.00"),
                "JUVENILE",
                List.of("IN_HEAT"),
                3,
                "HIGH",
                List.of("EATS_GRASS"),
                List.of("HIDING"),
                List.of("DISABILITY")));

        assertEquals("青年期", resp.ageGroupDesc());
        assertEquals(List.of("发情期"), resp.physiologicalStateDescs());
        assertEquals("高", resp.aggressionLevelDesc());
        assertEquals(List.of("食草"), resp.outdoorBehaviorDescs());
        assertEquals(List.of("易躲藏"), resp.indoorBehaviorDescs());
        assertEquals(List.of("肢体残疾"), resp.healthTagDescs());
    }

    @Test
    void rejectsConflictingPhysiologicalStates() {
        assertThrows(ClientException.class, () -> petProfileTagService.normalize(new PetProfileTagsReqDTO(
                null, null, List.of("NEUTERED", "IN_HEAT"), null, null, null, null, null)));
    }

    @Test
    void rejectsInvalidFriendliness() {
        assertThrows(ClientException.class, () -> petProfileTagService.normalize(new PetProfileTagsReqDTO(
                null, null, null, 6, null, null, null, null)));
    }

    @Test
    void emptyTagsNormalizeToNull() {
        assertNull(petProfileTagService.normalize(new PetProfileTagsReqDTO(
                null, null, null, null, null, null, null, null)));
    }
}
