package org.weinnovateit.client;

import java.io.BufferedReader;
import java.io.IOException;
import java.net.Socket;
import java.util.Date;

/**
 *
 * This class is responsible for reading the messages from the server and processing it. It does the following:
 *
 * 1. Reads the message from the server
 * 2. Processes the message (decoding the messages from the server)
 * 3. Prints the message to the console
 * 4. Sets the current state of the client (class)
 * Created by Jiggy (jig2nesh@gmail.com)
 *
 */


public class ServerMessageReaderThread implements Runnable{

    BufferedReader bufferedReaderFromSocket;
    Socket serverSocket;


    public boolean isLogoutConfirmationReceived() {
        return logoutConfirmationReceived;
    }

    private boolean logoutConfirmationReceived = false;



    private final MessageProcessor processor;

    public String getCommandList() {
        return commandList;
    }

    private String commandList = null;

    private String toUserUdpDetails = null;

    public String getAuthenticatedUser() {
        return authenticatedUser;
    }

    private String authenticatedUser = null;

    public ServerMessageReaderThread(BufferedReader bufferedReaderFromSocket, Socket serverSocket){
        this.bufferedReaderFromSocket = bufferedReaderFromSocket;
        this.serverSocket = serverSocket;
        processor = new MessageProcessor();
    }


    public String getToUserUdpDetails() {
        return toUserUdpDetails;
    }

    private void handleFetchResponse(String message){
        this.toUserUdpDetails = processor.getPrompt(message);
    }
    private void handleWelcomeMessage(String message) {
        this.authenticatedUser = processor.getAuthenticatedUserName(processor.getPrompt(message));
        String welcomeMessage = processor.getWelcomeMessage(processor.getPrompt(message));
        System.out.println(welcomeMessage);

    }

    private void handleCommandList(String message) {
        if(this.commandList == null)
            this.commandList = processor.getPrompt(message);
        System.out.println(processor.getPrompt(message));
    }

    private void handleActiveUsers(String message) {
        System.out.println(processor.getPrompt(message));
    }

    private void displayPrompt(String message){
        System.out.println(processor.getPrompt(message));
    }

    private void handleMsgTo(String message) {
        String status = processor.getPrompt(message);
        if (status.equalsIgnoreCase("Success")) {
            System.out.println("message sent at " + Config.dateFormat.format(new Date()));
        }
    }
    private void handleGroupMsgTo(String message) {
        String status = processor.getPrompt(message);
        if (status.equalsIgnoreCase("Success")) {
            System.out.println("Group chat message sent at " + Config.dateFormat.format(new Date()));
        }else
            this.displayPrompt(message);
    }



    private void handleMsgToContent(String message) {
        System.out.println(processor.getPrompt(message));
    }

    @Override
    public void run() {
        MessageProcessor processor = new MessageProcessor();
        try {
            String serverResponse;
            while ((serverResponse = this.bufferedReaderFromSocket.readLine()) != null && !serverSocket.isClosed()) {
                String serverCommand = processor.getServerCommand(serverResponse);
                switch (serverCommand) {
                    case "ALREADY_LOGGED_USER":
                    case "BLOCKED_USER":
                    case "BLOCKING_USER":
                        currentState = ClientState.BLOCKED;
                        System.out.println(processor.getPrompt(serverResponse));
                        break;
                    case "LOGOUT":
                        System.out.println(processor.getPrompt(serverResponse));
                        logoutConfirmationReceived = true;
                        break;
                    case "VALID_USERNAME":
                        currentState = ClientState.INVALID_PASSWORD;
                        break;
                    case "INVALID_PASSWORD":
                        currentState = ClientState.INVALID_PASSWORD;
                        this.displayPrompt(serverResponse);
                        break;
                    case "INVALID_USERNAME":
                        currentState = ClientState.INVALID_USERNAME;
                        this.displayPrompt(serverResponse);
                        break;
                    case "ACTIVE_USERS":
                        currentState = ClientState.LOGGED_IN_USER;
                        handleActiveUsers(serverResponse);
                        break;
                    case "MSGTO":
                        handleMsgTo(serverResponse);
                        break;
                    case "MSGTO_CONTENT":
                        handleMsgToContent(serverResponse);
                        break;
                    case "WELCOME_MESSAGE":
                        currentState = ClientState.LOGIN_SUCCESSFUL;
                        handleWelcomeMessage(serverResponse);
                        break;
                    case "COMMAND_LIST":
                        handleCommandList(serverResponse);
                        currentState = ClientState.LOGGED_IN_USER;
                        break;
                    case "GROUP_MSG":
                        handleGroupMsgTo(serverResponse);
                        currentState = ClientState.LOGGED_IN_USER;
                        break;
                    case "FETCH":
                        handleFetchResponse(serverResponse);
                        currentState = ClientState.LOGGED_IN_USER;
                        break;
                    case "FETCH_ERROR":
                        currentState = ClientState.LOGGED_IN_USER;
                        System.out.println(processor.getPrompt(serverResponse));
                        System.out.println(commandList);
                        break;
                    case "CREATE_GROUP":
                    case "JOIN_GROUP":
                    case "GROUP_MSG_CONTENT":
                    case "INVALID_COMMAND":
                        currentState = ClientState.LOGGED_IN_USER;
                        System.out.println(processor.getPrompt(serverResponse));
                        break;


                }
            }
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }



    public ClientState getCurrentState() {
        return currentState;
    }

    public void resetCurrentState(){
        this.currentState = ClientState.DEFAULT;
    }

    private ClientState currentState;


}
