package com.example.pets_backend.service.support;

import com.example.pets_backend.dto.req.OrderRequirementTagsReqDTO;
import com.example.pets_backend.dto.resp.OrderRequirementTagsRespDTO;
import com.example.pets_backend.enums.OrderRequirementTagEnum;
import com.example.pets_backend.frameworks.convention.errorcode.BaseErrorCode;
import com.example.pets_backend.frameworks.convention.exception.ClientException;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class OrderRequirementTagService {

    private final ObjectMapper objectMapper;

    public List<String> normalizeTags(List<String> rawTags) {
        if (rawTags == null || rawTags.isEmpty()) {
            return List.of();
        }
        Set<String> normalized = new LinkedHashSet<>();
        for (String tag : rawTags) {
            if (tag == null || tag.isBlank()) {
                continue;
            }
            OrderRequirementTagEnum knownTag = OrderRequirementTagEnum.fromCode(tag);
            if (knownTag == null) {
                throw new ClientException(BaseErrorCode.ORDER_REQUIREMENT_TAG_INVALID_ERROR);
            }
            normalized.add(knownTag.getCode());
        }
        return List.copyOf(normalized);
    }

    public String serialize(OrderRequirementTagsReqDTO reqDTO) {
        OrderRequirementSnapshot snapshot = buildSnapshot(reqDTO);
        if (snapshot.isEmpty()) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(snapshot);
        } catch (JsonProcessingException ex) {
            throw new ClientException(BaseErrorCode.SERVICE_ERROR);
        }
    }

    public OrderRequirementTagsRespDTO toRespDTO(String json) {
        return buildRespDTO(parseSnapshot(json), true);
    }

    public OrderRequirementTagsRespDTO toDetailRespDTO(String json) {
        return buildRespDTO(parseSnapshot(json), false);
    }

    private OrderRequirementTagsRespDTO buildRespDTO(
            OrderRequirementSnapshot snapshot, boolean includeAccessNote) {
        List<String> tags = snapshot.getTags();
        return new OrderRequirementTagsRespDTO(
                tags,
                OrderRequirementTagEnum.describeTags(tags),
                includeAccessNote ? snapshot.getAccessNote() : null,
                snapshot.getEmergencyContactName(),
                snapshot.getEmergencyContactPhone());
    }

    public boolean hasPlayCompanion(OrderRequirementTagsReqDTO reqDTO) {
        return containsTag(reqDTO, OrderRequirementTagEnum.NEED_PLAY_COMPANION);
    }

    public boolean hasCleaningService(OrderRequirementTagsReqDTO reqDTO) {
        return containsTag(reqDTO, OrderRequirementTagEnum.NEED_CLEANING);
    }

    public boolean containsTag(OrderRequirementTagsReqDTO reqDTO, OrderRequirementTagEnum tag) {
        if (reqDTO == null || reqDTO.tags() == null || reqDTO.tags().isEmpty() || tag == null) {
            return false;
        }
        return normalizeTags(reqDTO.tags()).contains(tag.getCode());
    }

    public OrderRequirementSnapshot parseSnapshot(String json) {
        if (json == null || json.isBlank()) {
            return new OrderRequirementSnapshot();
        }
        try {
            OrderRequirementSnapshot snapshot = objectMapper.readValue(json, OrderRequirementSnapshot.class);
            if (snapshot == null) {
                return new OrderRequirementSnapshot();
            }
            snapshot.setTags(normalizeTags(snapshot.getTags()));
            snapshot.setAccessNote(trimToNull(snapshot.getAccessNote()));
            snapshot.setEmergencyContactName(trimToNull(snapshot.getEmergencyContactName()));
            snapshot.setEmergencyContactPhone(trimToNull(snapshot.getEmergencyContactPhone()));
            return snapshot;
        } catch (JsonProcessingException ex) {
            return new OrderRequirementSnapshot();
        }
    }

    private OrderRequirementSnapshot buildSnapshot(OrderRequirementTagsReqDTO reqDTO) {
        if (reqDTO == null) {
            return new OrderRequirementSnapshot();
        }
        OrderRequirementSnapshot snapshot = new OrderRequirementSnapshot();
        snapshot.setTags(normalizeTags(reqDTO.tags()));
        snapshot.setAccessNote(trimToNull(reqDTO.accessNote()));
        snapshot.setEmergencyContactName(trimToNull(reqDTO.emergencyContactName()));
        snapshot.setEmergencyContactPhone(trimToNull(reqDTO.emergencyContactPhone()));
        return snapshot;
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    @Getter
    @Setter
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class OrderRequirementSnapshot {
        private List<String> tags = List.of();
        private String accessNote;
        private String emergencyContactName;
        private String emergencyContactPhone;

        public boolean isEmpty() {
            return (tags == null || tags.isEmpty())
                    && (accessNote == null || accessNote.isBlank())
                    && (emergencyContactName == null || emergencyContactName.isBlank())
                    && (emergencyContactPhone == null || emergencyContactPhone.isBlank());
        }
    }
}
