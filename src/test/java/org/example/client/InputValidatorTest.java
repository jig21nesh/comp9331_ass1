package org.example.client;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class InputValidatorTest {

    @Test
    void validateServerIpAddress() {
        String inputIpAddress = "localhost";
        InputValidator validator = new InputValidator();
        boolean isValid = validator.validateServerIpAddress(inputIpAddress);
        assertTrue(isValid);
    }

    @Test
    void shouldReturnFalseWhenNonLocalhostIpAddressIsProvided() {
        String inputIpAddress = "localhost1";
        InputValidator validator = new InputValidator();
        boolean isValid = validator.validateServerIpAddress(inputIpAddress);
        assertFalse(isValid);
    }

    @Test
    void shouldReturnFalseIfInputIsNotValidIPNumber() {
        String inputIpAddress = "256.256.256.257";
        InputValidator validator = new InputValidator();
        boolean isValid = validator.validateServerIpAddress(inputIpAddress);
        assertFalse(isValid);
    }

    @Test
    void shouldReturnValidPortWhenPortIsValidInt(){
        String inputPort = "12345";
        InputValidator validator = new InputValidator();
        boolean isValid = validator.isValidPort(inputPort);
        assertTrue(isValid);
    }

    @Test
    void shouldReturnFalseWhenPortValueIsNonInt(){
        String inputPort = "TEST";
        InputValidator validator = new InputValidator();
        boolean isValid = validator.isValidPort(inputPort);
        assertFalse(isValid);
    }
}