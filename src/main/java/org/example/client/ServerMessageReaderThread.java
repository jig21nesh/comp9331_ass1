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

    private boolean isWelcomeMessageReceived = false;

    public boolean isUserBlocked() {
        return isUserBlocked;
    }

    private boolean isUserBlocked = false;

    public boolean isInvalidPassword() {
        return isInvalidPassword;
    }

    private boolean isInvalidPassword = false;

    public boolean isInvalidUsername() {
        return isInvalidUsername;
    }

    private boolean isInvalidUsername = false;

    public ServerMessageReaderThread(BufferedReader bufferedReaderFromSocket, Socket serverSocket){
        this.bufferedReaderFromSocket = bufferedReaderFromSocket;
        this.serverSocket = serverSocket;
    }


    @Override
    public void run() {
        MessageProcessor processor = new MessageProcessor();
        try {
            String serverResponse;
            while ((serverResponse = this.bufferedReaderFromSocket.readLine()) != null && !serverSocket.isClosed()) {
                String serverCommand = processor.getServerCommand(serverResponse);
                if (serverCommand.equals("BLOCKING_USER")) {
                    currentState = ClientState.BLOCKED;
                } else if (serverCommand.contains("LOGOUT")) {
                    System.out.println(processor.getPrompt(serverResponse));
                    logoutConfirmationReceived = true;
                } else if (serverCommand.equals("WELCOME_MESSAGE") || serverCommand.equals("COMMAND_LIST")) {
                    currentState = ClientState.LOGIN_SUCCESSFUL;
                    if(serverCommand.equals("COMMAND_LIST"))
                        currentState = ClientState.LOGGED_IN_USER;
                    System.out.println(processor.getPrompt(serverResponse));
                } else if (serverCommand.equals("INVALID_PASSWORD")) {
                    currentState = ClientState.INVALID_PASSWORD;
                } else if (serverCommand.equals("INVALID_USERNAME")) {
                    currentState = ClientState.INVALID_USERNAME;
                }else if(serverCommand.contains("VALID_USERNAME")){
                    currentState = ClientState.INVALID_PASSWORD;
                }else if(serverCommand.equals("ACTIVE_USERS")){
                    currentState = ClientState.LOGGED_IN_USER;
                    System.out.println(processor.getPrompt(serverResponse));
                }else if(serverCommand.equals("MSGTO")){
                    String status = processor.getPrompt(serverResponse);
                    if(status.equalsIgnoreCase("Success")){
                        System.out.println("message sent at "+Config.dateFormat.format(new Date()));
                    }
                }else if(serverCommand.equals("MSGTO_CONTENT")){
                    System.out.println(processor.getPrompt(serverResponse));
                }
            }
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }

    public void setInvalidUsername(boolean b) {
        this.isInvalidUsername = b;
    }

    public boolean isWelcomeMessageReceived() {
        return this.isWelcomeMessageReceived;
    }

    public ClientState getCurrentState() {
        return currentState;
    }

    public void resetCurrentState(){
        this.currentState = ClientState.DEFAULT;
    }

    private ClientState currentState;


}
