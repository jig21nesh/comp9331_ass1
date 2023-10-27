package org.example.server;

import java.util.Base64;

public class MessagePDU {
    public String encodeString(MessageType messageType, String content){
        StringBuilder sb = new StringBuilder(messageType.getMetaData()).append(":").append(content);

        byte[] bytesToEncode = sb.toString().getBytes();
        byte[] encodedBytes = Base64.getEncoder().encode(bytesToEncode);

        // Limit the encoded string to no more than 5 characters
        return new String(encodedBytes);
    }

    public String[] decodeAndGetTypeAndContent(String encodedString){
        byte[] decodedBytes = Base64.getDecoder().decode(encodedString.getBytes());
        String plainText = new String(decodedBytes);
        return plainText.split(":");
    }


}
