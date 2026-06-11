package com.example.pets_backend.service.support;

import com.example.pets_backend.dto.req.PetProfileTagsReqDTO;
import com.example.pets_backend.dto.resp.PetProfileTagsRespDTO;
import com.example.pets_backend.enums.PetAgeGroupEnum;
import com.example.pets_backend.enums.PetAggressionLevelEnum;
import com.example.pets_backend.enums.PetHealthTagEnum;
import com.example.pets_backend.enums.PetIndoorBehaviorTagEnum;
import com.example.pets_backend.enums.PetOutdoorBehaviorTagEnum;
import com.example.pets_backend.enums.PetPhysiologicalStateEnum;
import com.example.pets_backend.frameworks.convention.errorcode.BaseErrorCode;
import com.example.pets_backend.frameworks.convention.exception.ClientException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PetProfileTagService {

    private static final BigDecimal MAX_WEIGHT_KG = new BigDecimal("200.00");

    private final ObjectMapper objectMapper;

    public PetProfileTagsReqDTO normalize(PetProfileTagsReqDTO raw) {
        if (raw == null) {
            return null;
        }
        BigDecimal weightKg = normalizeWeight(raw.weightKg());
        String ageGroup = normalizeAgeGroup(raw.ageGroup());
        List<String> physiologicalStates = normalizePhysiologicalStates(raw.physiologicalStates());
        Integer socialFriendliness = normalizeSocialFriendliness(raw.socialFriendliness());
        String aggressionLevel = normalizeAggressionLevel(raw.aggressionLevel());
        List<String> outdoorBehaviors = normalizeOutdoorBehaviors(raw.outdoorBehaviors());
        List<String> indoorBehaviors = normalizeIndoorBehaviors(raw.indoorBehaviors());
        List<String> healthTags = normalizeHealthTags(raw.healthTags());

        if (weightKg == null
                && ageGroup == null
                && physiologicalStates.isEmpty()
                && socialFriendliness == null
                && aggressionLevel == null
                && outdoorBehaviors.isEmpty()
                && indoorBehaviors.isEmpty()
                && healthTags.isEmpty()) {
            return null;
        }

        return new PetProfileTagsReqDTO(
                weightKg,
                ageGroup,
                physiologicalStates,
                socialFriendliness,
                aggressionLevel,
                outdoorBehaviors,
                indoorBehaviors,
                healthTags);
    }

    public String serialize(PetProfileTagsReqDTO tags) {
        PetProfileTagsReqDTO normalized = normalize(tags);
        if (normalized == null) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(normalized);
        } catch (JsonProcessingException ex) {
            throw new ClientException(BaseErrorCode.SERVICE_ERROR);
        }
    }

    public PetProfileTagsReqDTO parse(String json) {
        if (json == null || json.isBlank()) {
            return null;
        }
        try {
            PetProfileTagsReqDTO parsed = objectMapper.readValue(json, PetProfileTagsReqDTO.class);
            return normalize(parsed);
        } catch (JsonProcessingException ex) {
            return null;
        }
    }

    public PetProfileTagsRespDTO toRespDTO(PetProfileTagsReqDTO tags) {
        if (tags == null) {
            return null;
        }
        return new PetProfileTagsRespDTO(
                tags.weightKg(),
                tags.ageGroup(),
                PetAgeGroupEnum.getDescByCode(tags.ageGroup()),
                tags.physiologicalStates(),
                PetPhysiologicalStateEnum.describeTags(tags.physiologicalStates()),
                tags.socialFriendliness(),
                tags.aggressionLevel(),
                PetAggressionLevelEnum.getDescByCode(tags.aggressionLevel()),
                tags.outdoorBehaviors(),
                PetOutdoorBehaviorTagEnum.describeTags(tags.outdoorBehaviors()),
                tags.indoorBehaviors(),
                PetIndoorBehaviorTagEnum.describeTags(tags.indoorBehaviors()),
                tags.healthTags(),
                PetHealthTagEnum.describeTags(tags.healthTags()));
    }

    public PetProfileTagsRespDTO toRespDTO(String json) {
        return toRespDTO(parse(json));
    }

    private BigDecimal normalizeWeight(BigDecimal weightKg) {
        if (weightKg == null) {
            return null;
        }
        BigDecimal normalized = weightKg.setScale(2, RoundingMode.HALF_UP);
        if (normalized.compareTo(BigDecimal.ZERO) <= 0 || normalized.compareTo(MAX_WEIGHT_KG) > 0) {
            throw new ClientException(BaseErrorCode.PET_ARCHIVE_TAGS_INVALID_ERROR);
        }
        return normalized;
    }

    private String normalizeAgeGroup(String ageGroup) {
        if (ageGroup == null || ageGroup.isBlank()) {
            return null;
        }
        PetAgeGroupEnum known = PetAgeGroupEnum.fromCode(ageGroup);
        if (known == null) {
            throw new ClientException(BaseErrorCode.PET_ARCHIVE_TAGS_INVALID_ERROR);
        }
        return known.getCode();
    }

    private Integer normalizeSocialFriendliness(Integer socialFriendliness) {
        if (socialFriendliness == null) {
            return null;
        }
        if (socialFriendliness < 1 || socialFriendliness > 5) {
            throw new ClientException(BaseErrorCode.PET_ARCHIVE_TAGS_INVALID_ERROR);
        }
        return socialFriendliness;
    }

    private String normalizeAggressionLevel(String aggressionLevel) {
        if (aggressionLevel == null || aggressionLevel.isBlank()) {
            return null;
        }
        PetAggressionLevelEnum known = PetAggressionLevelEnum.fromCode(aggressionLevel);
        if (known == null) {
            throw new ClientException(BaseErrorCode.PET_ARCHIVE_TAGS_INVALID_ERROR);
        }
        return known.getCode();
    }

    private List<String> normalizePhysiologicalStates(List<String> tags) {
        List<String> normalized = normalizeTagList(tags, PetPhysiologicalStateEnum.values().length, code -> {
            if (PetPhysiologicalStateEnum.fromCode(code) == null) {
                throw new ClientException(BaseErrorCode.PET_ARCHIVE_TAGS_INVALID_ERROR);
            }
            return PetPhysiologicalStateEnum.fromCode(code).getCode();
        });
        if (normalized.contains(PetPhysiologicalStateEnum.NEUTERED.getCode())
                && normalized.contains(PetPhysiologicalStateEnum.IN_HEAT.getCode())) {
            throw new ClientException(BaseErrorCode.PET_ARCHIVE_TAGS_INVALID_ERROR);
        }
        return normalized;
    }

    private List<String> normalizeOutdoorBehaviors(List<String> tags) {
        return normalizeTagList(tags, PetOutdoorBehaviorTagEnum.values().length, code -> {
            if (PetOutdoorBehaviorTagEnum.fromCode(code) == null) {
                throw new ClientException(BaseErrorCode.PET_ARCHIVE_TAGS_INVALID_ERROR);
            }
            return PetOutdoorBehaviorTagEnum.fromCode(code).getCode();
        });
    }

    private List<String> normalizeIndoorBehaviors(List<String> tags) {
        return normalizeTagList(tags, PetIndoorBehaviorTagEnum.values().length, code -> {
            if (PetIndoorBehaviorTagEnum.fromCode(code) == null) {
                throw new ClientException(BaseErrorCode.PET_ARCHIVE_TAGS_INVALID_ERROR);
            }
            return PetIndoorBehaviorTagEnum.fromCode(code).getCode();
        });
    }

    private List<String> normalizeHealthTags(List<String> tags) {
        return normalizeTagList(tags, PetHealthTagEnum.values().length, code -> {
            if (PetHealthTagEnum.fromCode(code) == null) {
                throw new ClientException(BaseErrorCode.PET_ARCHIVE_TAGS_INVALID_ERROR);
            }
            return PetHealthTagEnum.fromCode(code).getCode();
        });
    }

    private List<String> normalizeTagList(List<String> tags, int maxSize, TagNormalizer normalizer) {
        if (tags == null || tags.isEmpty()) {
            return List.of();
        }
        Set<String> normalized = new LinkedHashSet<>();
        for (String tag : tags) {
            if (tag == null || tag.isBlank()) {
                continue;
            }
            normalized.add(normalizer.normalize(tag));
        }
        if (normalized.size() > maxSize) {
            throw new ClientException(BaseErrorCode.PET_ARCHIVE_TAGS_INVALID_ERROR);
        }
        return List.copyOf(normalized);
    }

    @FunctionalInterface
    private interface TagNormalizer {
        String normalize(String code);
    }
}
