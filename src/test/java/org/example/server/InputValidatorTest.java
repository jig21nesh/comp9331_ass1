package org.example.server;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class InputValidatorTest {

    @Test
    void shouldReturnAValidMessageWhenInputIsAValidString() {
        String inputPort = "12345";
        InputValidator validator = new InputValidator();
        boolean isValid = validator.validatePort(inputPort);
        assertTrue(isValid);
    }

    @Test
    void shouldReturnAFailureMessageWhenInputIsNonInteger() {
        String inputPort = "TEST";
        InputValidator validator = new InputValidator();
        boolean isValid = validator.validatePort(inputPort);
        assertFalse(isValid);
    }

    @Test
    void shouldReturnASuccessButWarningOnConsoleMessageWhenInputIsOutsideOfRange() {
        String inputPort = "60000";
        InputValidator validator = new InputValidator();
        boolean isValid = validator.validatePort(inputPort);
        assertTrue(isValid);
    }

    @Test
    void validatePort() {
    }
}