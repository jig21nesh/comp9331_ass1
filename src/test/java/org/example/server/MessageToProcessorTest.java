package org.example.server;

import org.junit.jupiter.api.Test;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class MessageToProcessorTest {

    @Test
    void shouldGetASuccessStatusIfInputIsValid() {
        Map<String, ActiveUser> userMap = new HashMap<>();
        userMap.put("test", new ActiveUser("test", new Date(), "localhost", 1234));
        MessageToProcessor messageToProcessor = new MessageToProcessor(userMap);
        MessageToProcessor.MessageToStatuses status = messageToProcessor.sendMessage("/msgto test hello");
        assertEquals(MessageToProcessor.MessageToStatuses.SUCCESS, status);
    }
}