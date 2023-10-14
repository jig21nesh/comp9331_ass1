package org.example.server;

import java.util.Arrays;
import java.util.Map;

public class MessageToProcessor {

    enum MessageToStatuses{
        INVALID_MESSAGE_TO_COMMAND(1, "Invalid Command"),
        USERNAME_NOT_FOUND_OR_NOT_ONLINE(2, "Username not found or not online"),

        INVALID_MESSAGE_CONTENT(3, "Invalid Message Content"),

        INVALID_INPUT(4, "Invalid Input"),

        SUCCESS(5, "Success");
        final int status;
        final String message;

        MessageToStatuses(int status, String message){
            this.status = status;
            this.message = message;
        }

        public int getStatus() {
            return status;
        }

        public String getMessage() {
            return message;
        }



    }

    private final Map<String, ActiveUser> activeUsersMap;

    public MessageToProcessor(Map<String, ActiveUser> activeUsersMap){
        this.activeUsersMap = activeUsersMap;
    }


    private MessageToStatuses isValidCommand(String input){
        String[] parts = input.split(" ");
        if (parts.length >= 3) {
            String command = parts[0];
            String username = parts[1];

            if(!command.equals("/msgto")){
                return MessageToStatuses.INVALID_MESSAGE_TO_COMMAND;
            }
            if(!activeUsersMap.containsKey(username)){
                return MessageToStatuses.USERNAME_NOT_FOUND_OR_NOT_ONLINE;
            }
            String message = String.join(" ", Arrays.copyOfRange(parts, 2, parts.length));
            if(!new InputValidator().clientMessage(message)){
                return MessageToStatuses.INVALID_MESSAGE_CONTENT;
            }
            return MessageToStatuses.SUCCESS;
        } else {
            return MessageToStatuses.INVALID_INPUT;
        }
    }
    public MessageToStatuses sendMessage(String input){
        MessageToStatuses status = this.isValidCommand(input);
        if(status == MessageToStatuses.SUCCESS){
            System.out.println("Message sent successfully");
        }
        System.out.println(status.getMessage());
        return status;
    }
}
