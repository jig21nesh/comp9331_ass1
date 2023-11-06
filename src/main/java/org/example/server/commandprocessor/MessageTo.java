package org.example.server.commandprocessor;

import org.example.server.*;
import org.example.server.logging.MessagesToFileWriter;

import java.io.PrintWriter;
import java.net.Socket;
import java.util.Arrays;
import java.util.Date;
import java.util.Map;

public class MessageTo {

    public String getUsername() {
        return this.username;
    }

    public String getMessage() {
        return this.message;
    }

    public enum MessageStatus{
        INVALID_MESSAGE_TO_COMMAND(1, "Invalid Command"),
        USERNAME_NOT_FOUND(2, "Not a valid username"),

        MESSAGE_TO_SELF(8, "Cannot send message to self"),

        USER_NOT_ONLINE(7, "User is not online"),

        INVALID_MESSAGE_CONTENT(3, "Invalid Message Content"),

        INVALID_INPUT(4, "Invalid Input"),

        FAILED_TO_SEND(6, "Failed to send message"),

        SUCCESS(5, "Success");

        final int status;
        final String statusMessage;

        MessageStatus(int status, String statusMessage){
            this.status = status;
            this.statusMessage = statusMessage;
        }

        public int getStatus() {
            return status;
        }

        public String getStatusMessage() {
            return statusMessage;
        }



    }

    private final Map<String, ActiveUser> activeUsersMap;

    private final CredentialValidator credentialValidator;

    public MessageTo(Map<String, ActiveUser> activeUsersMap, CredentialValidator credentialValidator){
        this.activeUsersMap = activeUsersMap;
        this.credentialValidator = credentialValidator;
    }


    private String command;
    private String username;
    private String message;

    private MessageStatus isValidCommand(String currentUser, String input){
        String[] parts = input.split(" ");
        if (parts.length >= 3) {
            String command = parts[0];
            String username = parts[1];

            if(!command.equals("/msgto")){
                return MessageStatus.INVALID_MESSAGE_TO_COMMAND;
            }
            if(!credentialValidator.isValidUsername(username)){
                return MessageStatus.USERNAME_NOT_FOUND;
            }
            if(!activeUsersMap.containsKey(username)){
                return MessageStatus.USER_NOT_ONLINE;
            }
            if(username.equals(currentUser)){
                return MessageStatus.MESSAGE_TO_SELF;
            }
            String message = String.join(" ", Arrays.copyOfRange(parts, 2, parts.length));
            if(!new InputValidator().clientMessage(message)){
                return MessageStatus.INVALID_MESSAGE_CONTENT;
            }

            this.command = command;
            this.username = username;
            this.message = message;

            return MessageStatus.SUCCESS;

        } else {
            return MessageStatus.INVALID_INPUT;
        }
    }

    private String formatSendMessage(String currentUser, String messageContent){
        return String.format("%s, %s: %s", Config.dateFormat.format(new Date()),currentUser, messageContent);
    }
    public MessageStatus sendMessage(String currentUser, String input){
        MessagesToFileWriter fileWriter = new MessagesToFileWriter();
        MessageStatus status = this.isValidCommand(currentUser, input);
        if(status == MessageStatus.SUCCESS){
            Socket socket = activeUsersMap.get(input.split(" ")[1]).getClientSocket();
            try{
                PrintWriter printWriter = new PrintWriter(socket.getOutputStream(), true);
                MessageTranslator messageProcessor = new MessageTranslator();
                printWriter.println(messageProcessor.encodeString(MessageTranslator.MessageType.MSGTO_CONTENT, this.formatSendMessage(currentUser, this.message)));
                printWriter.println(messageProcessor.encodeString(MessageTranslator.MessageType.COMMAND_LIST, SystemMessages.commandList()));
                fileWriter.writeToFile(this.username, this.message);
            }catch (Exception exception){
                status = MessageStatus.FAILED_TO_SEND;
            }

        }
        return status;
    }


}
