package com.example.pets_backend.service.support;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import com.example.pets_backend.dao.SitterProfileDao;
import com.example.pets_backend.dao.entity.OrderAddressSnapshotDO;
import com.example.pets_backend.dao.entity.OrderDO;
import com.example.pets_backend.dao.entity.SitterProfileDO;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class OrderHardFilterServiceTest {

    @Mock
    private SitterProfileDao sitterProfileDao;

    private OrderHardFilterService orderHardFilterService;

    @BeforeEach
    void setUp() {
        orderHardFilterService = new OrderHardFilterService(sitterProfileDao, new ObjectMapper());
    }

    @Test
    void normalizeTagsTreatsMissingAsEmpty() {
        assertTrue(orderHardFilterService.normalizeTags(null).isEmpty());
        assertTrue(orderHardFilterService.normalizeTags(List.of()).isEmpty());
        assertFalse(orderHardFilterService.hasHardFilterTags(null));
        assertTrue(orderHardFilterService.hasHardFilterTags(List.of("FEMALE_ONLY")));
    }

    @Test
    void femaleOnlyTagRequiresFemaleProvider() {
        SitterProfileDO maleProfile = buildProfile(1002L, 1, 31.23, 121.47, 5);
        SitterProfileDO femaleProfile = buildProfile(1003L, 2, 31.23, 121.47, 5);
        OrderDO order = buildOrder(2002L, List.of("FEMALE_ONLY"));
        OrderAddressSnapshotDO address = buildAddress(31.2300, 121.4700);

        assertFalse(orderHardFilterService.isProviderEligible(maleProfile, address, order));
        assertTrue(orderHardFilterService.isProviderEligible(femaleProfile, address, order));
    }

    @Test
    void providerOutsideServiceRadiusIsIneligible() {
        SitterProfileDO profile = buildProfile(1003L, 2, 31.30, 121.60, 5);
        OrderDO order = buildOrder(2002L, List.of("FEMALE_ONLY"));
        OrderAddressSnapshotDO address = buildAddress(31.2300, 121.4700);

        assertFalse(orderHardFilterService.isProviderEligible(profile, address, order));
    }

    @Test
    void businessTagsRequireMatchingCertLabels() {
        OrderDO order = buildOrder(2002L, List.of("ACCEPT_LARGE_DOG", "MEDICAL_FEEDING_EXPERIENCE"));
        OrderAddressSnapshotDO address = buildAddress(31.2300, 121.4700);

        SitterProfileDO withoutBusinessLabels = buildProfile(1002L, 2, 31.2304, 121.4737, 8, List.of("实名认证"));
        SitterProfileDO onlyLargeDog = buildProfile(1003L, 2, 31.2304, 121.4737, 8, List.of("接受大型犬"));
        SitterProfileDO both = buildProfile(1004L, 2, 31.2304, 121.4737, 8,
                List.of("接受大型犬", "具备医疗/喂药经验"));

        assertFalse(orderHardFilterService.isProviderEligible(withoutBusinessLabels, address, order));
        assertFalse(orderHardFilterService.isProviderEligible(onlyLargeDog, address, order));
        assertTrue(orderHardFilterService.isProviderEligible(both, address, order));
    }

    @Test
    void findEligibleProviderIdsFiltersByGenderAndRadius() {
        OrderDO order = buildOrder(2002L, List.of("FEMALE_ONLY"));
        order.setOwnerId(1001L);
        OrderAddressSnapshotDO address = buildAddress(31.2300, 121.4700);

        SitterProfileDO eligible = buildProfile(1003L, 2, 31.2304, 121.4737, 8);
        SitterProfileDO ineligibleGender = buildProfile(1002L, 1, 31.2304, 121.4737, 8);
        when(sitterProfileDao.selectVerifiedActiveProviders()).thenReturn(List.of(eligible, ineligibleGender));

        List<Long> providerIds = orderHardFilterService.findEligibleProviderIds(order, address);

        assertEquals(List.of(1003L), providerIds);
    }

    private OrderDO buildOrder(Long orderId, List<String> tags) {
        OrderDO order = new OrderDO();
        order.setOrderId(orderId);
        order.setOwnerId(1001L);
        if (tags == null || tags.isEmpty()) {
            order.setHardFilterTags(null);
        } else {
            order.setHardFilterTags("[\"" + String.join("\",\"", tags) + "\"]");
        }
        return order;
    }

    private SitterProfileDO buildProfile(Long providerId, int gender, double lat, double lng, int radiusKm) {
        return buildProfile(providerId, gender, lat, lng, radiusKm, List.of());
    }

    private SitterProfileDO buildProfile(Long providerId, int gender, double lat, double lng, int radiusKm,
            List<String> certLabels) {
        SitterProfileDO profile = new SitterProfileDO();
        profile.setProviderId(providerId);
        profile.setGender(gender);
        profile.setResidentLatitude(BigDecimal.valueOf(lat));
        profile.setResidentLongitude(BigDecimal.valueOf(lng));
        profile.setServiceRadiusKm(radiusKm);
        if (certLabels != null && !certLabels.isEmpty()) {
            profile.setCertLabelsJson("[\"" + String.join("\",\"", certLabels) + "\"]");
        }
        return profile;
    }

    private OrderAddressSnapshotDO buildAddress(double lat, double lng) {
        OrderAddressSnapshotDO address = new OrderAddressSnapshotDO();
        address.setLatitude(lat);
        address.setLongitude(lng);
        return address;
    }
}
