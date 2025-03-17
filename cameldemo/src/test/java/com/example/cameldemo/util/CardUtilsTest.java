package com.example.cameldemo.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CardUtilsTest {
    @Test
    void validCard() {
        assertTrue(CardUtils.isValidCard("4111111111111111"));
    }

    @Test
    void validCardWithSpacesPass() {
        assertTrue(CardUtils.isValidCard("4111 1111 1111 1111"));
    }

    @Test
    void invalidCardFailsLuhnFail() {
        assertFalse(CardUtils.isValidCard("4111111111111112"));
    }

    @Test
    void shortCardFail() {
        assertFalse(CardUtils.isValidCard("123"));
    }

    @Test
    void longCardFail() {
        assertFalse(CardUtils.isValidCard("41111111111111112222"));
    }

    @Test
    void nonDigitCharactersFail() {
        assertFalse(CardUtils.isValidCard("abcd1234abcd5678"));
    }

    @Test
    void emptyOrNullFail() {
        assertFalse(CardUtils.isValidCard(null));
        assertFalse(CardUtils.isValidCard(""));
    }

}