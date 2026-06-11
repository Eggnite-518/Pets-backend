package com.example.pets_backend.service.support;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.example.pets_backend.dto.req.OrderRequirementTagsReqDTO;
import com.example.pets_backend.frameworks.convention.exception.ClientException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class OrderRequirementTagServiceTest {

    private OrderRequirementTagService orderRequirementTagService;

    @BeforeEach
    void setUp() {
        orderRequirementTagService = new OrderRequirementTagService(new ObjectMapper());
    }

    @Test
    void serializeAndParseRequirementTags() {
        OrderRequirementTagsReqDTO reqDTO = new OrderRequirementTagsReqDTO(
                List.of("FOOD_BOWL_LOCATION", "ACCESS_CODE_LOCK", "VIDEO_ENTRY_CHECKIN"),
                "门禁密码 1234",
                "张三",
                "13800138000");

        String json = orderRequirementTagService.serialize(reqDTO);
        var resp = orderRequirementTagService.toRespDTO(json);

        assertEquals(3, resp.tags().size());
        assertEquals("食盆位置", resp.tagDescs().get(0));
        assertEquals("门禁密码 1234", resp.accessNote());
        assertEquals("张三", resp.emergencyContactName());
        assertEquals("13800138000", resp.emergencyContactPhone());
    }

    @Test
    void normalizeTagsRejectsUnknownCode() {
        assertThrows(ClientException.class,
                () -> orderRequirementTagService.normalizeTags(List.of("UNKNOWN_TAG")));
    }

    @Test
    void hasCleaningServiceDetectsTag() {
        assertTrue(orderRequirementTagService.hasCleaningService(
                new OrderRequirementTagsReqDTO(List.of("NEED_CLEANING"), null, null, null)));
    }

    @Test
    void hasPlayCompanionDetectsTag() {
        assertTrue(orderRequirementTagService.hasPlayCompanion(
                new OrderRequirementTagsReqDTO(List.of("NEED_PLAY_COMPANION"), null, null, null)));
        assertFalse(orderRequirementTagService.hasPlayCompanion(
                new OrderRequirementTagsReqDTO(List.of("FOOD_BOWL_LOCATION"), null, null, null)));
    }

    @Test
    void emptyRequirementReturnsNullJson() {
        assertTrue(orderRequirementTagService.serialize(new OrderRequirementTagsReqDTO(null, null, null, null)) == null);
    }
}
