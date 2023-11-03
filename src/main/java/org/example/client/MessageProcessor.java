package org.example.client;

import java.util.Base64;

public class MessageProcessor {

    private final String META_DATA_SEPARATOR = "<>";
    enum MessageType {


        ACTIVE_USERS("ACTIVE_USERS","Active users"),

        COMMAND("COMMAND","Command"),

        USERNAME("USERNAME","Username: "),
        BLOCKING_USER("BLOCKING_USER","Blocking user"),
        BLOCKED_USER("BLOCKED_USER","Blocked user"),
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
        byte[] bytesToEncode = (messageType.getMetaData() + META_DATA_SEPARATOR + content).getBytes();
        byte[] encodedBytes = Base64.getEncoder().encode(bytesToEncode);
        return new String(encodedBytes);
    }

    public String getServerCommand(String endcodedString){
        byte[] decodedBytes = Base64.getDecoder().decode(endcodedString.getBytes());
        String plainText = new String(decodedBytes);
        return plainText.split(META_DATA_SEPARATOR)[0];
    }

    public String getPrompt(String encodedString){
        byte[] decodedBytes = Base64.getDecoder().decode(encodedString.getBytes());
        String plainText = new String(decodedBytes);
        String messageType = plainText.split(META_DATA_SEPARATOR)[0];
        switch (messageType) {
            case "BLOCKING_USER":
                return MessageType.BLOCKING_USER.getPrompt();
            case "BLOCKED_USER":
                return MessageType.BLOCKED_USER.getPrompt();
            case "INVALID_USERNAME":
                return MessageType.INVALID_USERNAME.getPrompt();
            case "INVALID_PASSWORD":
                return MessageType.INVALID_PASSWORD.getPrompt();
            case "AUTH":
                return MessageType.AUTH_PROMPT.getPrompt();
            default:
                return plainText.split(META_DATA_SEPARATOR)[1];
        }
    }
}
