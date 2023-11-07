package org.example.server.commandprocessor;

import java.util.Base64;

public class MessageTranslator {

    private final String META_DATA_SEPARATOR = "<>";
    public enum MessageType {


        FETCH("FETCH","Fetch"),
        FETCH_ERROR("FETCH_ERROR","Fetch error"),
        INVALID_COMMAND("INVALID_COMMAND","Error. Invalid command!"),

        MSGTO_CONTENT("MSGTO_CONTENT","Message to content"),

        GROUP_MSG_CONTENT("GROUP_MSG_CONTENT","Group message content"),
        MSGTO("MSGTO","Message to"),

        GROUP_MSG("GROUP_MSG","Group message"),

        LOGOUT("LOGOUT","Logout"),
        BLOCKING_USER("BLOCKING_USER","Blocking user"),
        BLOCKED_USER("BLOCKED_USER","Blocked user"),
        INVALID_USERNAME("INVALID_USERNAME","Invalid username"),

        ALREADY_LOGGED_USER("ALREADY_LOGGED_USER","Already logged. Terminating this session"),
        INVALID_PASSWORD("INVALID_PASSWORD","Invalid password"),

        VALID_USERNAME("VALID_USERNAME","Valid username"),

        ACTIVE_USERS("ACTIVE_USERS","Active users"),

        CREATE_GROUP("CREATE_GROUP","Create group"),

        JOIN_GROUP("CREATE_GROUP","Create group"),

        COMMAND("COMMAND","Command"),

        WELCOME_MESSAGE("WELCOME_MESSAGE","Welcome"),

        COMMAND_LIST("COMMAND_LIST","Commands:"),
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

    public String getContent(String encodedString){
        byte[] decodedBytes = Base64.getDecoder().decode(encodedString.getBytes());
        String plainText = new String(decodedBytes);
        return plainText.split(META_DATA_SEPARATOR)[1];
    }
}
