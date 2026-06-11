package com.example.pets_backend.service.support;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.example.pets_backend.dao.entity.PetArchiveDO;
import com.example.pets_backend.dao.entity.UserAddressDO;
import com.example.pets_backend.dto.req.OrderRequirementTagsReqDTO;
import com.example.pets_backend.dto.resp.OrderServiceFeeQuoteRespDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class OrderServiceFeeCalculatorTest {

    private OrderServiceFeeCalculator calculator;

    @BeforeEach
    void setUp() {
        ObjectMapper objectMapper = new ObjectMapper();
        calculator = new OrderServiceFeeCalculator(
                new OrderRequirementTagService(objectMapper),
                new PetProfileTagService(objectMapper));
    }

    @Test
    void quoteUsesFirstTierCityAndWeekendSurcharge() {
        UserAddressDO address = buildAddress("上海");
        // 2026-06-06 is Saturday
        OrderServiceFeeQuoteRespDTO result = calculator.quote(
                address,
                LocalDate.of(2026, 6, 6),
                List.of(buildCat("团团")),
                null);

        assertEquals("50.00", result.totalAmount());
        assertEquals("50.00", findItem(result, "基础上门费").amount());
    }

    @Test
    void quoteAddsExtraPetLargeDogAndExoticFees() {
        UserAddressDO address = buildAddress("杭州");
        PetArchiveDO cat = buildCat("团团");
        PetArchiveDO largeDog = buildDog("虎子", "30.00");
        PetArchiveDO exotic = buildExotic("小仓");

        OrderServiceFeeQuoteRespDTO result = calculator.quote(
                address,
                LocalDate.of(2026, 6, 10),
                List.of(cat, largeDog, exotic),
                null);

        // 30 base + 10 extra pet + 5 large dog + 10 exotic = 55
        assertEquals("55.00", result.totalAmount());
        assertEquals(1, findItem(result, "多宠附加费").quantity());
        assertEquals(1, findItem(result, "大型犬附加费").quantity());
        assertEquals(1, findItem(result, "异宠附加费").quantity());
    }

    @Test
    void quoteAddsPlayAndCleaningServiceFees() {
        UserAddressDO address = buildAddress("杭州");
        OrderRequirementTagsReqDTO requirementTags = new OrderRequirementTagsReqDTO(
                List.of("NEED_PLAY_COMPANION", "NEED_CLEANING"),
                null,
                null,
                null);

        OrderServiceFeeQuoteRespDTO result = calculator.quote(
                address,
                LocalDate.of(2026, 6, 10),
                List.of(buildCat("团团")),
                requirementTags);

        // 30 + 5 play + 10 cleaning = 45
        assertEquals("45.00", result.totalAmount());
        assertEquals(1, findItem(result, "陪玩增值费").quantity());
        assertEquals(1, findItem(result, "清洁增值费").quantity());
    }

    @Test
    void quoteOnlyRecognizesFirstTierCities() {
        UserAddressDO address = buildAddress("成都");
        OrderServiceFeeQuoteRespDTO result = calculator.quote(
                address,
                LocalDate.of(2026, 6, 10),
                List.of(buildCat("团团")),
                null);

        assertEquals("30.00", result.totalAmount());
    }

    @Test
    void quoteRecognizesBeijingWhenStoredInProvince() {
        UserAddressDO address = new UserAddressDO();
        address.setProvince("北京市");
        address.setCity("朝阳区");

        OrderServiceFeeQuoteRespDTO result = calculator.quote(
                address,
                LocalDate.of(2026, 6, 10),
                List.of(buildCat("团团")),
                null);

        assertEquals("40.00", result.totalAmount());
        assertEquals("40.00", findItem(result, "基础上门费").amount());
    }

    @Test
    void quoteRecognizesShanghaiKeywordInCityOrProvince() {
        UserAddressDO address = new UserAddressDO();
        address.setProvince("上海市");
        address.setCity("");

        OrderServiceFeeQuoteRespDTO result = calculator.quote(
                address,
                LocalDate.of(2026, 6, 10),
                List.of(buildCat("团团")),
                null);

        assertEquals("40.00", result.totalAmount());
    }

    private OrderServiceFeeQuoteRespDTO.PriceItemRespDTO findItem(
            OrderServiceFeeQuoteRespDTO result, String itemName) {
        return result.priceItems().stream()
                .filter(item -> itemName.equals(item.itemName()))
                .findFirst()
                .orElseThrow();
    }

    private UserAddressDO buildAddress(String city) {
        UserAddressDO address = new UserAddressDO();
        address.setCity(city);
        return address;
    }

    private PetArchiveDO buildCat(String name) {
        PetArchiveDO pet = new PetArchiveDO();
        pet.setPetName(name);
        pet.setPetType(1);
        return pet;
    }

    private PetArchiveDO buildDog(String name, String weightJson) {
        PetArchiveDO pet = new PetArchiveDO();
        pet.setPetName(name);
        pet.setPetType(2);
        pet.setProfileTagsJson("{\"weightKg\":" + weightJson + "}");
        return pet;
    }

    private PetArchiveDO buildExotic(String name) {
        PetArchiveDO pet = new PetArchiveDO();
        pet.setPetName(name);
        pet.setPetType(3);
        return pet;
    }
}
