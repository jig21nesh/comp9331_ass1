package org.example.server;

import java.net.Socket;
import java.util.Date;

public class ActiveUser {
    private String username;
    private Date lastActive;


    public Socket getClientSocket() {
        return clientSocket;
    }

    private String ipAddress;

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    private int port;



    public void setClientSocket(Socket clientSocket) {
        this.clientSocket = clientSocket;
    }

    Socket clientSocket;

    public ActiveUser(Socket clientSocket, String username, Date lastActive){
        this.clientSocket = clientSocket;
        this.username = username;
        this.lastActive = lastActive;
        this.ipAddress = this.clientSocket.getInetAddress().getHostAddress();
        this.port = this.clientSocket.getPort();

    }

    public ActiveUser(Socket clientSocket, String username){
        this.clientSocket = clientSocket;
        this.username = username;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public Date getLastActive() {
        return lastActive;
    }

    public void setLastActive(Date lastActive) {
        this.lastActive = lastActive;
    }



    public String toString(){
        return "Username: "+this.username+" IP Address: "+this.ipAddress+" Port Number: "+this.getPort();
    }
}
