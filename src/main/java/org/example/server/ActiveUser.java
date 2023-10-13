package org.example.server;

import java.util.Date;

public class ActiveUser {
    private String username;
    private Date lastActive;

    private String ipAddress;

    private int portNumber;

    public ActiveUser(String username, Date lastActive, String ipAddress, int portNumber){
        this.username = username;
        this.lastActive = lastActive;
        this.ipAddress = ipAddress;
        this.portNumber = portNumber;
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

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public int getPortNumber() {
        return portNumber;
    }

    public void setPortNumber(int portNumber) {
        this.portNumber = portNumber;
    }

    public String toString(){
        return "Username: "+this.username+" IP Address: "+this.ipAddress+" Port Number: "+this.portNumber;
    }
}
