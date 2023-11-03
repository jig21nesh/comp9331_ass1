package org.example.client;

import java.util.Base64;

public class MessageProcessor {
    enum MessageType {

        USERNAME("USERNAME","Username: "),
        INVALID_USERNAME("INVALID_USERNAME","Invalid username"),
        INVALID_PASSWORD("INVALID_PASSWORD","Invalid password"),
        PASSWORD("PASSWORD","Password: "),
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

    public String getPrompt(String encodedString){
        byte[] decodedBytes = Base64.getDecoder().decode(encodedString.getBytes());
        String plainText = new String(decodedBytes);
        switch (plainText.split(":")[0]){
            case "AUTH":
                return MessageType.AUTH_PROMPT.getPrompt();
            default:
                return "Invalid message type";
        }
    }
}
