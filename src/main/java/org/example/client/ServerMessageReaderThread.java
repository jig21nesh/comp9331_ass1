package org.example.client;

import java.io.BufferedReader;
import java.io.IOException;
import java.net.Socket;
import java.util.Currency;

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
        try {
            String serverResponse;
            while ((serverResponse = this.bufferedReaderFromSocket.readLine()) != null && !serverSocket.isClosed()) {
                System.out.println(serverResponse);
                if (serverResponse.contains("blocked")) {
                    currentState = ClientState.BLOCKED;
                } else if (serverResponse.contains("Goodbye!")) {
                    logoutConfirmationReceived = true;
                } else if (serverResponse.contains("Welcome")) {
                    currentState = ClientState.LOGIN_SUCCESSFUL;
                } else if (serverResponse.contains("Invalid Password") && !serverResponse.contains("blocked")) {
                    currentState = ClientState.INVALID_PASSWORD;
                } else if (serverResponse.contains("username")) {
                    currentState = ClientState.INVALID_USERNAME;
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
