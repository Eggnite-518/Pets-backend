package com.example.pets_backend.service.support;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;

class IdCardGenderSupportTest {

    @Test
    void resolveGenderOddLastDigitAsMale() {
        assertEquals(IdCardGenderSupport.GENDER_MALE,
                IdCardGenderSupport.resolveGender("110101199001011235"));
    }

    @Test
    void resolveGenderEvenLastDigitAsFemale() {
        assertEquals(IdCardGenderSupport.GENDER_FEMALE,
                IdCardGenderSupport.resolveGender("110101199002021234"));
    }

    @Test
    void resolveGenderLastCharXAsFemale() {
        assertEquals(IdCardGenderSupport.GENDER_FEMALE,
                IdCardGenderSupport.resolveGender("11010119900101123X"));
    }

    @Test
    void resolveGenderInvalidLengthReturnsNull() {
        assertNull(IdCardGenderSupport.resolveGender("123456"));
    }
}
