package org.example.client;

import java.io.BufferedReader;
import java.io.IOException;
import java.net.Socket;

public class ServerMessageReaderThread implements Runnable{

    BufferedReader bufferedReaderFromSocket;
    Socket serverSocket;

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
            }
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }
}
