package org.example.client;

import java.io.BufferedReader;
import java.io.IOException;
import java.net.Socket;
import java.util.Currency;
import java.util.Date;

public class ServerMessageReaderThread implements Runnable{

    BufferedReader bufferedReaderFromSocket;
    Socket serverSocket;


    public boolean isLogoutConfirmationReceived() {
        return logoutConfirmationReceived;
    }

    private boolean logoutConfirmationReceived = false;

    private String authenticatedUsername;

    private final MessageProcessor processor;

    public ServerMessageReaderThread(BufferedReader bufferedReaderFromSocket, Socket serverSocket){
        this.bufferedReaderFromSocket = bufferedReaderFromSocket;
        this.serverSocket = serverSocket;
        processor = new MessageProcessor();
    }



    private void handleWelcomeMessage(String message) {
        String welcomeMessage = processor.getWelcomeMessage(processor.getPrompt(message));
        System.out.println(welcomeMessage);
        authenticatedUsername = processor.getAuthenticatedUserName(message);
    }

    private void handleCommandList(String message) {
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

    private void handleMsgToContent(String message) {
        System.out.println(processor.getPrompt(message));
    }

    @Override
    public void run() {
        MessageProcessor processor = new MessageProcessor();
        try {
            String serverResponse;
            while ((serverResponse = this.bufferedReaderFromSocket.readLine()) != null && !serverSocket.isClosed()) {
                System.out.println("Server response:: " + serverResponse);
                String serverCommand = processor.getServerCommand(serverResponse);

                switch (serverCommand) {
                    case "BLOCKED_USER":
                    case "BLOCKING_USER":
                        currentState = ClientState.BLOCKED;
                        System.out.println(processor.getPrompt(serverResponse));
                        break;
                    case "LOGOUT":
                        System.out.println(processor.getPrompt(serverResponse));
                        logoutConfirmationReceived = true;
                        break;
                    case "INVALID_PASSWORD":
                    case "VALID_USERNAME":
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
                        currentState = ClientState.LOGIN_SUCCESSFUL;
                        handleCommandList(serverResponse);
                        currentState = ClientState.LOGGED_IN_USER;
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
