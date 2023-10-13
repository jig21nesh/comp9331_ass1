package org.example.client;

import java.io.BufferedReader;
import java.io.IOException;
import java.net.Socket;

public class ServerMessageReaderThread implements Runnable{

    BufferedReader bufferedReaderFromSocket;
    Socket serverSocket;


    public boolean isLogoutConfirmationReceived() {
        return logoutConfirmationReceived;
    }

    private boolean logoutConfirmationReceived = false;

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
                if (serverResponse.contains("Goodbye!")) {
                    logoutConfirmationReceived = true;
                }
            }
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }
}
