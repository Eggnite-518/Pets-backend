package com.example.pets_backend.service.support;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.example.pets_backend.dao.entity.UserAddressDO;
import org.junit.jupiter.api.Test;

class FirstTierCityMatcherTest {

    @Test
    void matchesBeijingInProvinceEvenWhenCityIsDistrict() {
        UserAddressDO address = new UserAddressDO();
        address.setProvince("北京市");
        address.setCity("朝阳区");

        assertTrue(FirstTierCityMatcher.matches(address));
    }

    @Test
    void matchesShanghaiKeywordInProvinceOrCity() {
        UserAddressDO provinceOnly = new UserAddressDO();
        provinceOnly.setProvince("上海市");

        UserAddressDO cityOnly = new UserAddressDO();
        cityOnly.setCity("上海市");

        assertTrue(FirstTierCityMatcher.matches(provinceOnly));
        assertTrue(FirstTierCityMatcher.matches(cityOnly));
    }

    @Test
    void matchesKeywordInDetailAddressWhenStructuredFieldsEmpty() {
        UserAddressDO address = new UserAddressDO();
        address.setDetailAddress("北京市朝阳区望京街道");

        assertTrue(FirstTierCityMatcher.matches(address));
    }

    @Test
    void ignoresEmptyArrayPlaceholder() {
        assertFalse(FirstTierCityMatcher.containsKeyword("[]"));
    }

    @Test
    void doesNotMatchNonFirstTierCity() {
        UserAddressDO address = new UserAddressDO();
        address.setProvince("四川省");
        address.setCity("成都市");

        assertFalse(FirstTierCityMatcher.matches(address));
    }
}
