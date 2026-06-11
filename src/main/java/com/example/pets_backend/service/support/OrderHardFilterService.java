package com.example.pets_backend.service.support;

import com.example.pets_backend.dao.SitterProfileDao;
import com.example.pets_backend.dao.entity.OrderAddressSnapshotDO;
import com.example.pets_backend.dao.entity.OrderDO;
import com.example.pets_backend.dao.entity.SitterProfileDO;
import com.example.pets_backend.enums.OrderHardFilterTagEnum;
import com.example.pets_backend.frameworks.convention.errorcode.BaseErrorCode;
import com.example.pets_backend.frameworks.convention.exception.ClientException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class OrderHardFilterService {

    private static final int DEFAULT_SERVICE_RADIUS_KM = 5;

    private final SitterProfileDao sitterProfileDao;
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
            OrderHardFilterTagEnum knownTag = OrderHardFilterTagEnum.fromCode(tag);
            if (knownTag == null) {
                throw new ClientException(BaseErrorCode.ORDER_HARD_FILTER_INVALID_ERROR);
            }
            normalized.add(knownTag.getCode());
        }
        return List.copyOf(normalized);
    }

    /** 未传、null、空数组均视为无硬性门槛。 */
    public boolean hasHardFilterTags(List<String> rawTags) {
        return !normalizeTags(rawTags).isEmpty();
    }

    public String serializeTags(List<String> tags) {
        List<String> normalized = normalizeTags(tags);
        if (normalized.isEmpty()) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(normalized);
        } catch (JsonProcessingException ex) {
            throw new ClientException(BaseErrorCode.SERVICE_ERROR);
        }
    }

    public List<String> parseTags(String json) {
        if (json == null || json.isBlank()) {
            return List.of();
        }
        try {
            List<String> tags = objectMapper.readValue(json, new TypeReference<>() {
            });
            return tags == null ? List.of() : normalizeTags(tags);
        } catch (JsonProcessingException ex) {
            return List.of();
        }
    }

    public List<String> parseTags(OrderDO order) {
        return order == null ? List.of() : parseTags(order.getHardFilterTags());
    }

    public boolean isProviderEligible(SitterProfileDO profile, OrderAddressSnapshotDO address, OrderDO order) {
        if (profile == null || order == null) {
            return false;
        }
        return matchesHardFilterRequirements(profile, parseTags(order))
                && isWithinServiceRadius(profile, address);
    }

    public void ensureProviderEligible(SitterProfileDO profile, OrderAddressSnapshotDO address, OrderDO order) {
        if (!isProviderEligible(profile, address, order)) {
            throw new ClientException(BaseErrorCode.ORDER_HARD_FILTER_NOT_MATCH_ERROR);
        }
    }

    public List<Long> findEligibleProviderIds(OrderDO order, OrderAddressSnapshotDO address) {
        if (order == null) {
            return List.of();
        }
        List<String> tags = parseTags(order);
        List<SitterProfileDO> candidates = sitterProfileDao.selectVerifiedActiveProviders();
        if (candidates == null || candidates.isEmpty()) {
            return List.of();
        }
        List<Long> providerIds = new ArrayList<>();
        for (SitterProfileDO profile : candidates) {
            if (profile.getProviderId() == null) {
                continue;
            }
            if (Objects.equals(profile.getProviderId(), order.getOwnerId())) {
                continue;
            }
            if (matchesHardFilterRequirements(profile, tags) && isWithinServiceRadius(profile, address)) {
                providerIds.add(profile.getProviderId());
            }
        }
        return providerIds;
    }

    private boolean matchesHardFilterRequirements(SitterProfileDO profile, List<String> tags) {
        if (tags == null || tags.isEmpty()) {
            return true;
        }
        Set<String> providerCertLabels = parseProviderCertLabels(profile);
        for (String tagCode : tags) {
            OrderHardFilterTagEnum tag = OrderHardFilterTagEnum.fromCode(tagCode);
            if (tag == null) {
                continue;
            }
            if (tag.getRequiredGender() != null
                    && !Objects.equals(profile.getGender(), tag.getRequiredGender())) {
                return false;
            }
            if (tag.getRequiredCertLabel() != null
                    && !containsRequiredCertLabel(providerCertLabels, tag.getRequiredCertLabel())) {
                return false;
            }
        }
        return true;
    }

    private boolean containsRequiredCertLabel(Set<String> providerCertLabels, String requiredCertLabel) {
        if (providerCertLabels == null || providerCertLabels.isEmpty() || requiredCertLabel == null) {
            return false;
        }
        if (providerCertLabels.contains(requiredCertLabel)) {
            return true;
        }
        if ("具备医疗/喂药经验".equals(requiredCertLabel)) {
            return providerCertLabels.stream().anyMatch(label -> label.contains("医疗") || label.contains("喂药"));
        }
        if ("接受大型犬".equals(requiredCertLabel)) {
            return providerCertLabels.stream().anyMatch(label -> label.contains("大型犬"));
        }
        return false;
    }

    private Set<String> parseProviderCertLabels(SitterProfileDO profile) {
        if (profile == null || profile.getCertLabelsJson() == null || profile.getCertLabelsJson().isBlank()) {
            return Set.of();
        }
        try {
            List<String> labels = objectMapper.readValue(profile.getCertLabelsJson(), new TypeReference<>() {
            });
            if (labels == null || labels.isEmpty()) {
                return Set.of();
            }
            Set<String> normalized = new LinkedHashSet<>();
            for (String label : labels) {
                if (label == null || label.isBlank()) {
                    continue;
                }
                normalized.add(label.trim());
            }
            return normalized;
        } catch (JsonProcessingException ex) {
            return Set.of();
        }
    }

    private boolean isWithinServiceRadius(SitterProfileDO profile, OrderAddressSnapshotDO address) {
        if (address == null || profile == null) {
            return false;
        }
        Double orderLat = address.getLatitude();
        Double orderLng = address.getLongitude();
        Double providerLat = profile.getResidentLatitude() == null ? null : profile.getResidentLatitude().doubleValue();
        Double providerLng = profile.getResidentLongitude() == null ? null : profile.getResidentLongitude().doubleValue();
        if (orderLat == null || orderLng == null || providerLat == null || providerLng == null) {
            return false;
        }
        Double distanceKm = GeoUtils.distanceKm(orderLat, orderLng, providerLat, providerLng);
        if (distanceKm == null) {
            return false;
        }
        int radiusKm = profile.getServiceRadiusKm() == null ? DEFAULT_SERVICE_RADIUS_KM : profile.getServiceRadiusKm();
        if (radiusKm <= 0) {
            return true; // 0 = 不限距离
        }
        return distanceKm <= radiusKm;
    }
}
