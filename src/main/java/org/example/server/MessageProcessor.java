package org.example.server;

import java.util.Base64;

public class MessageProcessor {
    enum MessageType {

        USERNAME("USERNAME","Username: "),
        PASSWORD("PASSWORD","Password: "),
        INVALID_USERNAME("INVALID_USERNAME","Invalid username"),
        INVALID_PASSWORD("INVALID_PASSWORD","Invalid password"),
        AUTH_PROMPT("AUTH","Please login");

        private final String metaData;
        private final String prompt;

        MessageType(String metaData, String prompt){
            this.metaData = metaData;
            this.prompt = prompt;
        }

        public String getMetaData() {
            return metaData;
        }

        public String getPrompt() {
            return prompt;
        }
    }

    public String encodeString(MessageType messageType, String content){
        byte[] bytesToEncode = (messageType.getMetaData() + ":" + content).getBytes();
        byte[] encodedBytes = Base64.getEncoder().encode(bytesToEncode);
        return new String(encodedBytes);
    }

    public String getContent(String encodedString){
        byte[] decodedBytes = Base64.getDecoder().decode(encodedString.getBytes());
        String plainText = new String(decodedBytes);
        return plainText.split(":")[1];
    }
}
